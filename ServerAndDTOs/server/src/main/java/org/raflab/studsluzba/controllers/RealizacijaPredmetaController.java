package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.RealizacijaPredmetaDTO;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.RealizacijaPredmetaService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/realizacija")
@RequiredArgsConstructor
public class RealizacijaPredmetaController {

    private final RealizacijaPredmetaService service;
    private final CurrentUser currentUser;

    @PostMapping("/generate")
    public List<RealizacijaPredmetaDTO> generate(@RequestParam Long programId,
                                                 @RequestParam(required = false) Long skolskaGodinaId) {
        currentUser.requireAdmin();
        return service.generateForProgram(programId, skolskaGodinaId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping("/all")
    public List<RealizacijaPredmetaDTO> all(@RequestParam(required = false) Long skolskaGodinaId) {
        currentUser.requireAdmin();
        return service.all(skolskaGodinaId).stream().map(this::toDto).collect(Collectors.toList());
    }

    private RealizacijaPredmetaDTO toDto(RealizacijaPredmeta r) {
        var pp = r.getProgramPredmet();
        return new RealizacijaPredmetaDTO(
                r.getId(), pp.getId(), pp.getProgram().getId(), pp.getProgram().getOznaka(),
                pp.getPredmet().getId(), pp.getPredmet().getSifra(), pp.getPredmet().getNaziv(),
                pp.getGodinaStudija(), pp.getSemestarUGodini(),
                r.getSkolskaGodina().getId(), r.getSkolskaGodina().getGodina(), r.getStatus().name()
        );
    }
}
