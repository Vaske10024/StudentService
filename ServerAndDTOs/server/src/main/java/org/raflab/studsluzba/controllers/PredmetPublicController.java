package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.PredmetDTO;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/predmet")
@RequiredArgsConstructor
public class PredmetPublicController {

    private final PredmetRepository predmetRepo;

    @GetMapping("/all")
    public java.util.List<PredmetDTO> all() {
        return StreamSupport.stream(predmetRepo.findAll().spliterator(), false)
                .map(p -> new PredmetDTO(
                        p.getId(),
                        p.getSifra(),
                        p.getNaziv(),
                        p.getOpis(),
                        p.getEspb(),
                        p.getStudProgram() != null ? p.getStudProgram().getOznaka() : null
                ))
                .collect(Collectors.toList());
    }
}
