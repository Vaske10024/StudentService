package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.PredmetDTO;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/predmet")
@RequiredArgsConstructor
public class PredmetController {

    private final PredmetRepository predmetRepo;

    @GetMapping("/{id}")
    public PredmetDTO byId(@PathVariable Long id) {
        Predmet p = predmetRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeći predmet: " + id));
        return toDto(p);
    }
    private PredmetDTO toDto(Predmet p) {
        String oznaka = null;
        try {
            if (p.getStudProgram() != null) oznaka = p.getStudProgram().getOznaka();
        } catch (Exception ignored) { }

        return new PredmetDTO(
                p.getId(),
                p.getSifra(),
                p.getNaziv(),
                p.getOpis(),
                p.getEspb(),
                oznaka
        );
    }
}
