package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.FinanceBalanceDTO;
import org.raflab.studsluzba.model.dtos.LedgerEntryDTO;
import org.raflab.studsluzba.model.finance.LedgerEntry;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.services.PermissionService;
import org.raflab.studsluzba.services.FinanceService;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/finance")
@RequiredArgsConstructor
public class FinanceController {
    private final FinanceService service;
    private final CurrentUser currentUser;
    private final PermissionService permissions;

    @GetMapping("/{indeksId}/balance")
    public FinanceBalanceDTO balance(@PathVariable Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return service.balance(indeksId);
    }

    @GetMapping("/{indeksId}/ledger")
    public List<LedgerEntryDTO> ledger(@PathVariable Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return service.ledgerDto(indeksId);
    }

    @PostMapping("/{indeksId}/obligations")
    public LedgerEntryDTO obligation(@PathVariable Long indeksId, @RequestParam BigDecimal amountEur,
                                  @RequestParam LocalDate dueDate, @RequestParam String type) {
        permissions.require(Permission.FINANCE_WRITE);
        return toDto(service.createObligation(indeksId, amountEur, dueDate, type));
    }

    @PostMapping("/{indeksId}/payments")
    public LedgerEntryDTO payment(@PathVariable Long indeksId, @RequestParam BigDecimal amountEur,
                               @RequestParam(required = false) String description) {
        permissions.require(Permission.FINANCE_WRITE);
        return toDto(service.postPayment(indeksId, amountEur, description));
    }

    @PostMapping("/entries/{entryId}/reverse")
    public LedgerEntryDTO reverse(@PathVariable Long entryId, @RequestParam String reason) {
        permissions.require(Permission.FINANCE_WRITE);
        return toDto(service.reverse(entryId, reason));
    }

    private LedgerEntryDTO toDto(LedgerEntry item) {
        return new LedgerEntryDTO(item.getId(), item.getType().name(), item.getAmountEur(),
                item.getDescription(), item.isReversed(), item.getCreatedAt());
    }
}
