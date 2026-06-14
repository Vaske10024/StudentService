package org.raflab.studsluzba.services;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.Uplata;
import org.raflab.studsluzba.model.dtos.SaldoResponse;
import org.raflab.studsluzba.model.dtos.UplataDTO;
import org.raflab.studsluzba.model.dtos.FinanceBalanceDTO;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.UplataRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UplataService {

    private final UplataRepository uplataRepo;
    private final StudentIndeksRepository indeksRepo;
    private final FinanceService financeService;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<LocalDate, ExchangeRate> rateCache = new ConcurrentHashMap<>();

    @Value("${tuition.default.eur:3000.00}")
    private BigDecimal defaultTuitionEur;

    @Value("${payment.exchange-rate.fallback-eur-rsd:117.00}")
    private BigDecimal fallbackRate;

    @Value("${payment.exchange-rate.api-url:https://kurs.resenje.org/api/v1/currencies/eur/rates/today}")
    private String exchangeRateApiUrl;

    @Transactional
    public Long dodajUplatu(Long indeksId, BigDecimal iznosRsd, BigDecimal kursOverride, LocalDate datumOverride) {
        StudentIndeks si = indeksRepo.findById(indeksId)
                .orElseThrow(() -> new NoSuchElementException("Indeks ne postoji: " + indeksId));
        if (iznosRsd == null || iznosRsd.compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.badRequest("Iznos uplate mora biti veći od 0.");
        }

        LocalDate datum = datumOverride != null ? datumOverride : LocalDate.now();
        ExchangeRate rate = kursOverride != null
                ? new ExchangeRate(kursOverride, false)
                : fetchKurs(datum);
        if (rate.getValue() == null || rate.getValue().compareTo(BigDecimal.ZERO) <= 0) {
            throw ApiException.conflict("Kurs EUR mora biti veći od 0.");
        }

        Uplata u = new Uplata();
        u.setIndeks(si);
        u.setDatum(datum);
        u.setIznosRsd(iznosRsd.setScale(2, RoundingMode.HALF_UP));
        u.setSrednjiKursEur(rate.getValue().setScale(6, RoundingMode.HALF_UP));
        u.setFallbackKurs(rate.isFallback());
        Uplata saved = uplataRepo.save(u);
        financeService.postPayment(indeksId, toEur(saved), "Legacy RSD payment #" + saved.getId());
        return saved.getId();
    }

    @Transactional(readOnly = true)
    public List<Uplata> list(Long indeksId) {
        return uplataRepo.findByIndeksId(indeksId);
    }

    @Transactional(readOnly = true)
    public SaldoResponse saldo(Long indeksId) {
        StudentIndeks si = indeksRepo.findById(indeksId)
                .orElseThrow(() -> new NoSuchElementException("Indeks ne postoji: " + indeksId));
        List<Uplata> uplate = uplataRepo.findByIndeksId(indeksId);

        BigDecimal ukupnoRsd = uplate.stream()
                .map(u -> u.getIznosRsd() == null ? BigDecimal.ZERO : u.getIznosRsd())
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal ukupnoEur = uplate.stream()
                .map(this::toEur)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        FinanceBalanceDTO ledgerBalance = financeService.balance(indeksId);
        BigDecimal preostaloEur = ledgerBalance.getDebtEur().setScale(2, RoundingMode.HALF_UP);
        ExchangeRate current = fetchKurs(LocalDate.now());
        BigDecimal preostaloRsd = preostaloEur.multiply(current.getValue()).setScale(2, RoundingMode.HALF_UP);

        boolean fallbackUsed = current.isFallback() || uplate.stream().anyMatch(Uplata::isFallbackKurs);
        return new SaldoResponse(ukupnoEur, ukupnoRsd, preostaloEur, preostaloRsd,
                current.getValue().setScale(6, RoundingMode.HALF_UP), fallbackUsed);
    }

    @Transactional(readOnly = true)
    public List<UplataDTO> listDto(Long indeksId) {
        return uplataRepo.findByIndeksId(indeksId).stream()
                .map(u -> new UplataDTO(
                        u.getId(),
                        u.getDatum(),
                        u.getIznosRsd(),
                        u.getSrednjiKursEur(),
                        u.isFallbackKurs(),
                        u.getIndeks() != null ? u.getIndeks().getId() : null
                ))
                .collect(Collectors.toList());
    }

    private BigDecimal toEur(Uplata u) {
        if (u.getIznosRsd() == null || u.getSrednjiKursEur() == null || u.getSrednjiKursEur().compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        return u.getIznosRsd().divide(u.getSrednjiKursEur(), 8, RoundingMode.HALF_UP);
    }

    private BigDecimal tuitionFor(StudentIndeks si) {
        // Extension point: replace with TuitionRepository keyed by school year, study program and financing type.
        return defaultTuitionEur == null ? BigDecimal.ZERO : defaultTuitionEur.setScale(2, RoundingMode.HALF_UP);
    }

    private ExchangeRate fetchKurs(LocalDate datum) {
        return rateCache.computeIfAbsent(datum, ignored -> fetchKursUncached());
    }

    private ExchangeRate fetchKursUncached() {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(5))
                    .build();
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(exchangeRateApiUrl))
                    .timeout(Duration.ofSeconds(5))
                    .GET()
                    .build();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() < 200 || resp.statusCode() >= 300) {
                throw new IllegalStateException("Exchange rate API returned status " + resp.statusCode());
            }
            JsonNode root = objectMapper.readTree(resp.body());
            JsonNode middleNode = root.get("exchange_middle");
            if (middleNode != null && middleNode.isNumber()) {
                return new ExchangeRate(new BigDecimal(middleNode.asText()), false);
            }
            throw new IllegalStateException("Exchange rate API response does not contain exchange_middle.");
        } catch (Exception e) {
            log.warn("EUR exchange rate API failed; using configured fallback rate {}. Cause: {}", fallbackRate, e.toString());
            return new ExchangeRate(fallbackRate, true);
        }
    }

    @Getter
    @AllArgsConstructor
    private static class ExchangeRate {
        private BigDecimal value;
        private boolean fallback;
    }
}
