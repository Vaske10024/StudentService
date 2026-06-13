package org.raflab.studsluzba.controllers;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.SlusaPredmetDTO;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.SlusaPredmetService;
import org.raflab.studsluzba.utils.EntityMappers;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/slusa")
@RequiredArgsConstructor
@Validated
public class SlusaPredmetController {

    private final SlusaPredmetService service;
    private final CurrentUser currentUser;
    private final EntityMappers entityMappers;

    @PostMapping("/create")
    public Long add(@RequestBody @Validated AddReq req) {
        currentUser.requireAdmin();
        return service.add(req.getIndeksId(), req.getDrziPredmetId());
    }

    @DeleteMapping("/{id}")
    public void remove(@PathVariable Long id) {
        currentUser.requireAdmin();
        service.remove(id);
    }

    @GetMapping("/by-indeks")
    public List<SlusaPredmetDTO> byIndeks(@RequestParam Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return service.byIndeks(indeksId).stream()
                .map(entityMappers::fromSlusaPredmetToDTO)
                .collect(Collectors.toList());
    }

    @Data
    public static class AddReq {
        @NotNull private Long indeksId;
        @NotNull private Long drziPredmetId;
    }
}
