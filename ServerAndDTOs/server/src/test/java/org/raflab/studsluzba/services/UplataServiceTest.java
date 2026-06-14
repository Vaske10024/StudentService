package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.Uplata;
import org.raflab.studsluzba.model.dtos.SaldoResponse;
import org.raflab.studsluzba.model.dtos.FinanceBalanceDTO;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.UplataRepository;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UplataServiceTest {
    @Test
    void saldoUsesBigDecimalAndStoredExchangeRate() {
        UplataRepository uplataRepo = mock(UplataRepository.class);
        StudentIndeksRepository indeksRepo = mock(StudentIndeksRepository.class);
        FinanceService financeService = mock(FinanceService.class);
        UplataService service = new UplataService(uplataRepo, indeksRepo, financeService);
        ReflectionTestUtils.setField(service, "defaultTuitionEur", new BigDecimal("1000.00"));
        ReflectionTestUtils.setField(service, "fallbackRate", new BigDecimal("100.00"));
        ReflectionTestUtils.setField(service, "exchangeRateApiUrl", "not-a-uri");

        StudentIndeks indeks = new StudentIndeks();
        indeks.setId(5L);
        Uplata uplata = new Uplata();
        uplata.setId(1L);
        uplata.setIndeks(indeks);
        uplata.setDatum(LocalDate.now());
        uplata.setIznosRsd(new BigDecimal("10000.00"));
        uplata.setSrednjiKursEur(new BigDecimal("100.000000"));
        uplata.setFallbackKurs(false);

        when(indeksRepo.findById(5L)).thenReturn(Optional.of(indeks));
        when(uplataRepo.findByIndeksId(5L)).thenReturn(Collections.singletonList(uplata));
        when(financeService.balance(5L)).thenReturn(new FinanceBalanceDTO(new BigDecimal("900.00"), new BigDecimal("900.00"), BigDecimal.ZERO));

        SaldoResponse saldo = service.saldo(5L);

        assertThat(saldo.getUkupnoRsd()).isEqualByComparingTo("10000.00");
        assertThat(saldo.getUkupnoEur()).isEqualByComparingTo("100.00");
        assertThat(saldo.getPreostaloEur()).isEqualByComparingTo("900.00");
        assertThat(saldo.getPreostaloRsd()).isEqualByComparingTo("90000.00");
        assertThat(saldo.isFallbackRateUsed()).isTrue();
    }
}
