package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.VisokaSkola;
import org.raflab.studsluzba.model.dtos.VisokaSkolaDTO;
import org.raflab.studsluzba.model.dtos.VisokaSkolaRequest;
import org.raflab.studsluzba.services.VisokaSkolaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/visoka")
@RequiredArgsConstructor
@Validated
public class VisokaSkolaController {

    private final VisokaSkolaService service;

    @PostMapping
    public Long create(@RequestBody @Validated VisokaSkolaRequest req) {
        return service.create(req.getNaziv(), req.getMesto(), req.getTip());
    }

    @GetMapping("/all")
    public List<VisokaSkolaDTO> all() {
        return service.all().stream().map(this::toDto).collect(Collectors.toList());
    }

    private VisokaSkolaDTO toDto(VisokaSkola v) {
        return new VisokaSkolaDTO(v.getId(), v.getNaziv(), v.getMesto(), v.getTip());
    }
}
