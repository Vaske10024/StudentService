package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.dtos.PolozenPredmetDTO;
import org.raflab.studsluzba.model.dtos.PredmetDTO;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.StudentIspitiViewService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/ispiti/view")
@RequiredArgsConstructor
public class StudentIspitiViewController {

    private final StudentIspitiViewService service;
    private final StudentIndeksRepository indeksRepo;
    private final CurrentUser currentUser;

    @GetMapping("/polozeni")
    public Page<PolozenPredmetDTO> polozene(
            @RequestParam String sp,
            @RequestParam int godina,
            @RequestParam int broj,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        currentUser.requireAdminOrStudentOwnsIndeks(resolveIndeksId(sp, godina, broj));
        return service.polozenePaged(sp, godina, broj, page, size);
    }

    @GetMapping("/nepolozeni")
    public Page<PredmetDTO> nepolozeni(
            @RequestParam String sp,
            @RequestParam int godina,
            @RequestParam int broj,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        currentUser.requireAdminOrStudentOwnsIndeks(resolveIndeksId(sp, godina, broj));
        return service.nepolozeniPaged(sp, godina, broj, page, size);
    }

    private Long resolveIndeksId(String sp, int godina, int broj) {
        StudentIndeks si = indeksRepo.findStudentIndeks(sp, godina, broj);
        if (si == null) throw new NoSuchElementException("Ne postoji indeks: " + sp + " " + godina + " " + broj);
        return si.getId();
    }
}
