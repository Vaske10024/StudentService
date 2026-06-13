package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.PolozenPredmetDTO;
import org.raflab.studsluzba.model.dtos.StudentPodaciResponse;
import org.raflab.studsluzba.services.StudentIspitiViewService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(path = "/api/student/query")
@RequiredArgsConstructor
public class StudentQueryController {

    private final StudentQueryFacade facade;
    private final StudentIspitiViewService ispitiViewService;

    @GetMapping("/by-index")
    public StudentPodaciResponse byIndex(@RequestParam String sp, @RequestParam int godina, @RequestParam int broj) {
        return facade.findPodaciByIndex(sp, godina, broj);
    }

    @GetMapping("/polozeni")
    public List<PolozenPredmetDTO> polozeni(@RequestParam String sp, @RequestParam int godina, @RequestParam int broj) {
        return ispitiViewService.polozenePaged(sp, godina, broj, 0, 1000).getContent();
    }

    @GetMapping("/po-srednjoj")
    public List<StudentPodaciResponse> poSrednjoj(@RequestParam String naziv) {
        return facade.findBySrednja(naziv);
    }
}
