package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.OstvarenaPredObavService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/predispit")
@RequiredArgsConstructor
public class PredispitController {

    private final OstvarenaPredObavService ostvarenaService;
    private final CurrentUser currentUser;

    @GetMapping("/poeni")
    public Integer predispitni(@RequestParam Long studentIndeksId,
                               @RequestParam Long predmetId,
                               @RequestParam Long skolskaGodinaId) {
        currentUser.requireAdminOrStudentOwnsIndeks(studentIndeksId);
        return ostvarenaService.ukupniPoeni(studentIndeksId, predmetId, skolskaGodinaId);
    }
}
