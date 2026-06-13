package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.PredmetAdminCreateRequest;
import org.raflab.studsluzba.model.dtos.PredmetAdminUpdateRequest;
import org.raflab.studsluzba.model.dtos.PredmetDTO;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.raflab.studsluzba.services.PredmetAdminService;
import org.raflab.studsluzba.utils.EntityMappers;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/predmet/admin")
@RequiredArgsConstructor
@Validated
public class PredmetAdminController {

    private final PredmetAdminService service;
    private final PredmetRepository predmetRepo;
    private final EntityMappers entityMappers;

    @PostMapping("/create")
    public Long create(@RequestBody @Validated PredmetAdminCreateRequest req) {
        return service.create(req.getSifra(), req.getNaziv(), req.getOpis(), req.getEspb(), req.getProgramId(),
                req.isObavezan(), req.getGodinaStudija(), req.getSemestarUGodini());
    }

    @PatchMapping("/{id}")
    public void update(@PathVariable Long id, @RequestBody PredmetAdminUpdateRequest req) {
        service.update(id, req.getNaziv(), req.getOpis(), req.getEspb());
    }

    @GetMapping("/all/full")
    public List<PredmetDTO> allFull() {
        return StreamSupport.stream(predmetRepo.findAll().spliterator(), false)
                .map(entityMappers::fromPredmetToDTO)
                .collect(Collectors.toList());
    }
}
