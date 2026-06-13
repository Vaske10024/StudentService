package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.SaldoResponse;
import org.raflab.studsluzba.model.dtos.UplataDTO;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.UplataService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/uplate")
@RequiredArgsConstructor
public class UplataController {

    private final UplataService service;
    private final CurrentUser currentUser;

    @PostMapping("/dodaj")
    public Long dodaj(@RequestParam Long indeksId, @RequestParam BigDecimal iznosRsd) {
        currentUser.requireAdmin();
        return service.dodajUplatu(indeksId, iznosRsd, null, null);
    }

    @GetMapping("/saldo")
    public SaldoResponse saldo(@RequestParam Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return service.saldo(indeksId);
    }

    @GetMapping("/list")
    public List<UplataDTO> list(@RequestParam Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return service.listDto(indeksId);
    }
}
