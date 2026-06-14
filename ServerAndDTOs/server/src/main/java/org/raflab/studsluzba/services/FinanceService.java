package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.dtos.FinanceBalanceDTO;
import org.raflab.studsluzba.model.dtos.LedgerEntryDTO;
import org.raflab.studsluzba.model.finance.FinancialObligation;
import org.raflab.studsluzba.model.finance.LedgerEntry;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.finance.FinancialObligationRepository;
import org.raflab.studsluzba.repositories.finance.LedgerEntryRepository;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FinanceService {
    private final LedgerEntryRepository ledgerRepo;
    private final FinancialObligationRepository obligationRepo;
    private final StudentIndeksRepository indeksRepo;
    private final PaymentAllocationService allocationService;
    private final CurrentUser currentUser;
    private final AuditLogRepository auditRepo;

    public LedgerEntry createObligation(Long indeksId, BigDecimal amountEur, LocalDate dueDate, String type) {
        requirePositive(amountEur);
        StudentIndeks indeks = requireIndeks(indeksId);
        FinancialObligation obligation = new FinancialObligation();
        obligation.setStudentIndeks(indeks);
        obligation.setAmountEur(amountEur);
        obligation.setDueDate(dueDate);
        obligation.setType(type == null ? "OTHER" : type);
        obligationRepo.save(obligation);
        return post(indeks, LedgerEntry.Type.CHARGE, amountEur, "obligationId=" + obligation.getId());
    }

    public LedgerEntry postPayment(Long indeksId, BigDecimal amountEur, String description) {
        requirePositive(amountEur);
        LedgerEntry payment = post(requireIndeks(indeksId), LedgerEntry.Type.PAYMENT, amountEur.negate(), description);
        allocationService.allocateOldestFirst(payment);
        return payment;
    }

    public LedgerEntry reverse(Long entryId, String reason) {
        if (reason == null || reason.trim().isEmpty()) throw ApiException.badRequest("Razlog storna je obavezan.");
        LedgerEntry original = ledgerRepo.findById(entryId).orElseThrow(() -> ApiException.notFound("Ledger stavka ne postoji: " + entryId));
        if (original.isReversed() || original.getType() == LedgerEntry.Type.REVERSAL) {
            throw ApiException.conflict("LEDGER_ENTRY_ALREADY_REVERSED", "Ledger stavka je vec stornirana.");
        }
        if (original.getType() == LedgerEntry.Type.PAYMENT) {
            allocationService.reversePaymentAllocations(original);
        }
        original.setReversed(true);
        ledgerRepo.save(original);
        LedgerEntry reversal = post(original.getStudentIndeks(), LedgerEntry.Type.REVERSAL,
                original.getAmountEur().negate(), "reversalOf=" + entryId + ", reason=" + reason.trim());
        reversal.setReversesEntry(original);
        ledgerRepo.save(reversal);
        audit("LEDGER_ENTRY_REVERSED", "entryId=" + entryId);
        return reversal;
    }

    @Transactional(readOnly = true)
    public FinanceBalanceDTO balance(Long indeksId) {
        BigDecimal balance = ledgerRepo.findByStudentIndeksIdOrderByCreatedAtAsc(indeksId).stream()
                .map(LedgerEntry::getAmountEur).reduce(BigDecimal.ZERO, BigDecimal::add);
        return new FinanceBalanceDTO(balance, balance.max(BigDecimal.ZERO), balance.min(BigDecimal.ZERO).abs());
    }

    @Transactional(readOnly = true)
    public List<LedgerEntry> ledger(Long indeksId) {
        return ledgerRepo.findByStudentIndeksIdOrderByCreatedAtAsc(indeksId);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryDTO> ledgerDto(Long indeksId) {
        return ledger(indeksId).stream()
                .map(item -> new LedgerEntryDTO(item.getId(), item.getType().name(), item.getAmountEur(),
                        item.getDescription(), item.isReversed(), item.getCreatedAt()))
                .collect(java.util.stream.Collectors.toList());
    }

    private LedgerEntry post(StudentIndeks indeks, LedgerEntry.Type type, BigDecimal amount, String description) {
        LedgerEntry entry = new LedgerEntry();
        entry.setStudentIndeks(indeks);
        entry.setType(type);
        entry.setAmountEur(amount);
        entry.setDescription(description);
        entry.setActorUserId(currentUser.userId());
        return ledgerRepo.save(entry);
    }

    private StudentIndeks requireIndeks(Long id) {
        return indeksRepo.findById(id).orElseThrow(() -> ApiException.notFound("Indeks ne postoji: " + id));
    }

    private void requirePositive(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) throw ApiException.badRequest("Iznos mora biti veci od nule.");
    }

    private void audit(String action, String details) {
        AuditLog log = new AuditLog();
        log.setActorUserId(currentUser.userId());
        log.setAction(action);
        log.setDetails(details);
        auditRepo.save(log);
    }
}
