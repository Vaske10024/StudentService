package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.PredispitCreateRequest;
import org.raflab.studsluzba.model.dtos.PredispitOstvarenoRequest;
import org.raflab.studsluzba.model.dtos.PredispitnaObavezaDTO;
import org.raflab.studsluzba.model.ispiti.PredispitnaObaveza;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.OstvarenaPredObavService;
import org.raflab.studsluzba.services.PredispitnaObavezaService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Min;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/predispit/admin")
@RequiredArgsConstructor
@Validated
public class PredispitAdminController {

    private final PredispitnaObavezaService defService;
    private final OstvarenaPredObavService ostvarenaService;
    private final CurrentUser currentUser;

    @PostMapping("/definicija")
    public Long createDef(@RequestBody @Validated PredispitCreateRequest req) {
        currentUser.requireAdmin();
        return defService.create(req.getPredmetId(), req.getSkolskaGodinaId(), req.getVrsta(), req.getMaxPoeni());
    }

    @PatchMapping("/definicija/{id}/max")
    public void updateMax(@PathVariable Long id, @RequestParam @Min(0) int max) {
        currentUser.requireAdmin();
        defService.updateMax(id, max);
    }

    @DeleteMapping("/definicija/{id}")
    public void deleteDef(@PathVariable Long id) {
        currentUser.requireAdmin();
        defService.delete(id);
    }

    @GetMapping("/definicije")
    public List<PredispitnaObavezaDTO> definicije(@RequestParam Long predmetId, @RequestParam Long skolskaGodinaId) {
        return defService.list(predmetId, skolskaGodinaId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @PostMapping("/ostvareno")
    public Long upsert(@RequestBody @Validated PredispitOstvarenoRequest req) {
        currentUser.requireAdminOrProfessorOwnsPredispitnaObaveza(req.getPredispitnaObavezaId());
        return ostvarenaService.upsert(req.getStudentIndeksId(), req.getPredispitnaObavezaId(), req.getPoeni());
    }

    @GetMapping("/ostvareno/detalji")
    public List<OstvarenaPredObavService.DetaljDTO> detalji(@RequestParam Long studentIndeksId,
                                                            @RequestParam Long predmetId,
                                                            @RequestParam Long skolskaGodinaId) {
        return ostvarenaService.listDetalji(studentIndeksId, predmetId, skolskaGodinaId);
    }

    private PredispitnaObavezaDTO toDto(PredispitnaObaveza po) {
        return new PredispitnaObavezaDTO(
                po.getId(),
                po.getVrsta(),
                po.getMaxPoeni(),
                po.getPredmet() == null ? null : po.getPredmet().getId(),
                po.getPredmet() == null ? null : po.getPredmet().getNaziv(),
                po.getSkolskaGodina() == null ? null : po.getSkolskaGodina().getId(),
                po.getSkolskaGodina() == null ? null : po.getSkolskaGodina().getGodina()
        );
    }
}
