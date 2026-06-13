package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.SkolskaGodinaDTO;
import org.raflab.studsluzba.model.dtos.SkolskaGodinaRequest;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.services.SkolskaGodinaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/sg")
@RequiredArgsConstructor
@Validated
public class SkolskaGodinaController {

    private final SkolskaGodinaService service;

    @PostMapping
    public Long create(@RequestBody @Validated SkolskaGodinaRequest req) {
        return service.create(req.getOznaka(), Boolean.TRUE.equals(req.getAktivna()));
    }

    @PatchMapping("/{id}/aktiviraj")
    public void activate(@PathVariable Long id) {
        service.activate(id);
    }

    // FIX: DTO umesto entity
    @GetMapping("/aktivna")
    public SkolskaGodinaDTO active() {
        return toDto(service.active());
    }

    // FIX: DTO umesto entity
    @GetMapping("/all")
    public List<SkolskaGodinaDTO> all() {
        return StreamSupport.stream(service.all().spliterator(), false)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private SkolskaGodinaDTO toDto(SkolskaGodina sg) {
        if (sg == null) return null;
        return new SkolskaGodinaDTO(
                sg.getId(),
                sg.getGodina(),
                sg.isAktivna()
        );
    }
}
