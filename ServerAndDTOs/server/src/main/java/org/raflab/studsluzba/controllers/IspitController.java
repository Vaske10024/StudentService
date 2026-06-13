package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.IspitCommandService;
import org.raflab.studsluzba.services.IspitQueryService;
import org.raflab.studsluzba.utils.IspitMappers;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(path = "/api/ispit")
@RequiredArgsConstructor
public class IspitController {

    private final IspitQueryService query;
    private final IspitCommandService command;
    private final IspitMappers ispitMappers;
    private final CurrentUser currentUser;

    @GetMapping("/{ispitId}/prijavljeni")
    public List<StudentLiteDTO> prijavljeni(@PathVariable Long ispitId) {
        currentUser.requireAdminOrProfessorOwnsIspit(ispitId);
        return query.listaPrijavljenih(ispitId)
                .stream()
                .map(ispitMappers::toStudentLiteDTO)
                .collect(Collectors.toList());
    }

    @GetMapping("/{ispitId}/prosek")
    public Double prosek(@PathVariable Long ispitId) {
        currentUser.requireAdminOrProfessorOwnsIspit(ispitId);
        return query.prosecnaOcena(ispitId);
    }

    @GetMapping("/{ispitId}/rezultati")
    public List<IspitRezultatDTO> rezultati(@PathVariable Long ispitId) {
        currentUser.requireAdminOrProfessorOwnsIspit(ispitId);
        return query.rezultatiSortiraniDTO(ispitId);
    }

    @GetMapping("/{ispitId}/prijave")
    public List<PrijavaResponseDTO> prijave(@PathVariable Long ispitId) {
        currentUser.requireAdminOrProfessorOwnsIspit(ispitId);
        return query.aktivnePrijaveDTO(ispitId);
    }

    @GetMapping("/predmet/{predmetId}/prosek-raspon")
    public Double prosecnaOcenaZaPredmetURasponu(@PathVariable Long predmetId,
                                                 @RequestParam int fromYear,
                                                 @RequestParam int toYear) {
        currentUser.requireAdmin();
        if (fromYear > toYear) {
            throw new IllegalArgumentException("fromYear mora biti <= toYear.");
        }
        return query.prosekZaPredmetURasponu(predmetId, fromYear, toYear);
    }

    @GetMapping("/broj-polaganja")
    public Long brojPolaganja(@RequestParam Long studentIndeksId, @RequestParam Long predmetId) {
        currentUser.requireAdminOrStudentOwnsIndeks(studentIndeksId);
        return query.brojPolaganja(studentIndeksId, predmetId);
    }

    @PostMapping("/prijava")
    public Long kreirajPrijavu(@RequestBody @Valid PrijavaCreateRequest req) {
        currentUser.requireAdminOrStudentOwnsIndeks(req.getStudentIndeksId());
        return command.prijaviStudenta(req);
    }

    @PatchMapping("/prijava/rezultat")
    public Long azurirajRezultat(@RequestBody @Valid PrijavaResultUpdateRequest req) {
        currentUser.requireAdminOrProfessorOwnsPrijava(req.getPrijavaId());
        return command.azurirajRezultat(req);
    }

    @PatchMapping("/prijava/{id}/ponisti")
    public void ponisti(@PathVariable Long id, @RequestBody @Valid CancellationRequest request) {
        currentUser.requireAdminOrProfessorOwnsPrijava(id);
        command.ponisti(id, request.getReason());
    }

    @PatchMapping("/prijava/{id}/odjavi")
    public void odjavi(@PathVariable Long id, @RequestBody @Valid CancellationRequest request) {
        command.odjavi(id, request.getReason());
    }

    @PostMapping("/priznaj")
    public Long priznaj(@RequestParam Long studentIndeksId,
                        @RequestParam Long predmetId,
                        @RequestParam Integer ocena,
                        @RequestParam(required = false) String napomena) {
        currentUser.requireAdmin();
        return command.priznajPredmet(studentIndeksId, predmetId, ocena, napomena);
    }

    @PostMapping("/izlazak")
    public Long evidentirajIzlazak(@RequestBody @Valid IspitIzlazakRequest req) {
        currentUser.requireAdminOrProfessorOwnsPrijava(req.getPrijavaId());
        return command.evidentirajIzlazak(req);
    }
}
