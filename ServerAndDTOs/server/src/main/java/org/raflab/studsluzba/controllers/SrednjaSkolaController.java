package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.SrednjaSkola;
import org.raflab.studsluzba.model.dtos.SrednjaSkolaDTO;
import org.raflab.studsluzba.model.dtos.SrednjaSkolaRequest;
import org.raflab.studsluzba.services.SrednjaSkolaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/srednja")
@RequiredArgsConstructor
@Validated
public class SrednjaSkolaController {

    private final SrednjaSkolaService service;

    @PostMapping
    public Long create(@RequestBody @Validated SrednjaSkolaRequest req) {
        return service.create(req.getNaziv(), req.getMesto(), req.getVrsta());
    }

    @GetMapping("/all")
    public List<SrednjaSkolaDTO> all() {
        return service.all().stream().map(this::toDto).collect(Collectors.toList());
    }

    private SrednjaSkolaDTO toDto(SrednjaSkola s) {
        return new SrednjaSkolaDTO(s.getId(), s.getNaziv(), s.getMesto(), s.getVrsta());
    }
}
