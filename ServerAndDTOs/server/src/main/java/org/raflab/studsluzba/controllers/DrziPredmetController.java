package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.dtos.DrziPredmetCreateRequest;
import org.raflab.studsluzba.model.dtos.DrziPredmetLiteDTO;
import org.raflab.studsluzba.model.dtos.StudentIndeksLiteDTO;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.SlusaPredmetRepository;
import org.raflab.studsluzba.services.DrziPredmetAdminService;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/drzi")
@RequiredArgsConstructor
@Validated
public class DrziPredmetController {

    private final DrziPredmetAdminService service;
    private final SlusaPredmetRepository slusaRepo;
    private final DrziPredmetRepository repo;
    private final CurrentUser currentUser;

    @GetMapping("/all")
    public List<DrziPredmetLiteDTO> all() {
        List<DrziPredmet> assignments = currentUser.isAdmin()
                ? repo.findAllAktivnaWithRefs()
                : repo.findActiveByNastavnikId(currentUser.linkedNastavnikId());
        return assignments.stream()
                .map(this::toLite)
                .collect(Collectors.toList());
    }

    private DrziPredmetLiteDTO toLite(DrziPredmet dp) {
        DrziPredmetLiteDTO dto = new DrziPredmetLiteDTO();
        dto.setId(dp.getId());
        dto.setPredmetId(dp.getPredmet().getId());
        dto.setPredmetNaziv(dp.getPredmet().getNaziv());
        dto.setNastavnikId(dp.getNastavnik().getId());
        dto.setNastavnikImePrezime(dp.getNastavnik().getIme() + " " + dp.getNastavnik().getPrezime());
        if (dp.getRealizacijaPredmeta() != null) {
            dto.setRealizacijaPredmetaId(dp.getRealizacijaPredmeta().getId());
            dto.setProgramOznaka(dp.getRealizacijaPredmeta().getProgramPredmet().getProgram().getOznaka());
        }
        dto.setUloga(dp.getUloga() == null ? null : dp.getUloga().name());
        if (dp.getSkolskaGodina() != null) {
            dto.setSkolskaGodinaId(dp.getSkolskaGodina().getId());
            dto.setSkolskaGodinaNaziv(dp.getSkolskaGodina().getGodina());
        }
        return dto;
    }

    @PostMapping("/create")
    public Long create(@RequestBody @Validated DrziPredmetCreateRequest req) {
        currentUser.requireAdmin();
        return service.createOne(req.getPredmetId(), req.getNastavnikId(), req.getSkolskaGodinaId(),
                req.getRealizacijaPredmetaId(), req.getUloga());
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        currentUser.requireAdmin();
        service.delete(id);
    }

    // FIX: DTO umesto modela (StudentIndeks)
    @GetMapping("/{drziPredmetId}/studenti")
    public List<StudentIndeksLiteDTO> studenti(@PathVariable Long drziPredmetId) {
        if (!currentUser.isAdmin()) {
            currentUser.requireProfessorOwnsDrziPredmet(drziPredmetId);
        }
        return slusaRepo.getStudentiSlusaPredmetZaDrziPredmet(drziPredmetId).stream()
                .map(this::toIndeksLite)
                .collect(Collectors.toList());
    }

    private StudentIndeksLiteDTO toIndeksLite(StudentIndeks si) {
        StudentIndeksLiteDTO dto = new StudentIndeksLiteDTO();
        dto.setId(si.getId());
        dto.setBroj(si.getBroj());
        dto.setGodina(si.getGodina());
        dto.setStudProgramOznaka(si.getStudProgramOznaka());
        if (si.getStudent() != null) {
            dto.setStudentId(si.getStudent().getId());
            dto.setIme(si.getStudent().getIme());
            dto.setPrezime(si.getStudent().getPrezime());
        }
        return dto;
    }
}
