package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.NastavnikZvanje;
import org.raflab.studsluzba.model.dtos.NastavnikZvanjeCreateRequest;
import org.raflab.studsluzba.model.dtos.NastavnikZvanjeDTO;
import org.raflab.studsluzba.services.NastavnikZvanjeService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/nastavnik/zvanje")
@RequiredArgsConstructor
@Validated
public class NastavnikZvanjeController {

    private final NastavnikZvanjeService service;

    @PostMapping
    public Long add(@RequestBody @Validated NastavnikZvanjeCreateRequest req) {
        return service.add(req.getNastavnikId(), req.getZvanje(), req.getNaucnaOblast(),
                req.getUzaNaucnaOblast(), req.getDatumIzbora(), Boolean.TRUE.equals(req.getAktivno()));
    }

    // FIX: DTO umesto entity
    @GetMapping("/list")
    public List<NastavnikZvanjeDTO> list(@RequestParam Long nastavnikId,
                                         @RequestParam(defaultValue = "false") boolean onlyAktivno) {
        return service.list(nastavnikId, onlyAktivno).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PatchMapping("/{zvanjeId}/aktiviraj")
    public void activate(@PathVariable Long zvanjeId) {
        service.activate(zvanjeId);
    }

    private NastavnikZvanjeDTO toDto(NastavnikZvanje nz) {
        if (nz == null) return null;
        return new NastavnikZvanjeDTO(
                nz.getId(),
                nz.getZvanje(),
                nz.getNaucnaOblast(),
                nz.getUzaNaucnaOblast(),
                nz.getDatumIzbora(),
                nz.isAktivno()
        );
    }
}
