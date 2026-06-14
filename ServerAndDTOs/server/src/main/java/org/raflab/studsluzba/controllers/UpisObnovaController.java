package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.ObnovaCreateRequest;
import org.raflab.studsluzba.model.dtos.UpisCreateRequest;
import org.raflab.studsluzba.model.dtos.UpisanaGodinaDTO;
import org.raflab.studsluzba.services.UpisObnovaService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/studij")
@RequiredArgsConstructor
public class UpisObnovaController {

    private final UpisObnovaService service;


    //upis student 16 je  id 4

    //Upisi studenta na godinu
    @PostMapping("/upis")
    public Long upis(@RequestBody @Valid UpisCreateRequest req) {
        return service.upisi(req);
    }

    @PostMapping("/sync-subjects")
    public int syncSubjects(@RequestParam Long indeksId) {
        return service.syncCurrentSubjects(indeksId);
    }

    @PostMapping("/obnova")
    public Long obnova(@RequestBody @Valid ObnovaCreateRequest req) {
        return service.obnova(req);
    }



    //Sve upisane godine
    @GetMapping("/upisi")
    public List<UpisanaGodinaDTO> upisi(@RequestParam Long indeksId) {
        return service.readUpisi(indeksId).stream()
                .map(u -> new UpisanaGodinaDTO(
                        u.getId(),
                        "UPIS",
                        u.getUpisujeGodinu(),
                        u.getDatum(),
                        u.getNapomena(),
                        u.getSkolskaGodina() == null ? null : u.getSkolskaGodina().getGodina()
                ))
                .collect(Collectors.toList());
    }

    @GetMapping("/obnove")
    public List<UpisanaGodinaDTO> obnove(@RequestParam Long indeksId) {
        return service.readObnoveDto(indeksId);
    }

    @GetMapping("/godine")
    public List<UpisanaGodinaDTO> sveGodineZaIndeks(
            @RequestParam String sp,
            @RequestParam int godina,
            @RequestParam int broj
    ) {
        return service.pregledZaBrojIndeksa(sp, godina, broj);
    }
}
