package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.model.dtos.PredmetOnProgramDTO;
import org.raflab.studsluzba.services.ProgramService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path="/api/program")
@RequiredArgsConstructor
public class ProgramController {

    private final ProgramService service;

    @GetMapping("/{programId}/predmeti")
    public List<PredmetOnProgramDTO> predmeti(@PathVariable Long programId) {
        List<ProgramPredmet> list = service.predmeti(programId);

        return list.stream()
                .map(pp -> new PredmetOnProgramDTO(
                        pp.getPredmet().getId(),
                        pp.getPredmet().getSifra(),
                        pp.getPredmet().getNaziv(),
                        pp.getPredmet().getOpis(),
                        pp.getPredmet().getEspb(),
                        pp.getProgram().getId(),
                        pp.getSemestarUkupno(),
                        pp.getGodinaStudija(),
                        pp.getSemestarUGodini(),
                        pp.getFondPredavanja(),
                        pp.getFondVezbi(),
                        pp.getFondPraktikum()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/predmet/{predmetId}/prosek")
    public Double prosekZaRaspon(@PathVariable Long predmetId,
                                 @RequestParam int fromYear,
                                 @RequestParam int toYear) {
        return service.prosekZaPredmetURasponu(predmetId, fromYear, toYear);
    }

    @GetMapping("/{programId}/predmeti/full")
    public List<PredmetOnProgramDTO> predmetiFull(@PathVariable Long programId) {
        return predmeti(programId);
    }
}
