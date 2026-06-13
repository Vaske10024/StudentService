package org.raflab.studsluzba.controllers;


import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.ProgramPredmetCreateRequest;
import org.raflab.studsluzba.services.ProgramCommandService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/program")
@RequiredArgsConstructor
public class ProgramCommandController {

    private final ProgramCommandService service;

    @PostMapping("/{programId}/predmeti/add")
    public Long addPredmet(@PathVariable Long programId, @RequestBody @Valid ProgramPredmetCreateRequest req) {
        return service.addPredmetNaProgram(programId, req);
    }
}
