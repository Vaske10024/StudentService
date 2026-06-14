package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.IspitDTO;
import org.raflab.studsluzba.model.dtos.IspitniRokCreateRequest;
import org.raflab.studsluzba.model.dtos.IspitniRokDTO;
import org.raflab.studsluzba.model.dtos.IspitniRokUpdateRequest;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.services.IspitniRokService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@RestController
@RequestMapping("/api/rok")
@RequiredArgsConstructor
@Validated
public class IspitniRokController {

    private final IspitniRokService service;
    private final IspitRepository ispitRepo;

    @PostMapping("/create")
    public Long create(@RequestBody @Validated IspitniRokCreateRequest req) {
        return service.create(req);
    }

    @PutMapping("/{id}")
    public IspitniRokDTO update(@PathVariable Long id, @RequestBody @Validated IspitniRokUpdateRequest req) {
        return toRokDTO(service.update(id, req));
    }

    // ✅ DTO lista - nema lazy serijalizacije
    @GetMapping("/all")
    public List<IspitniRokDTO> all() {
        return StreamSupport.stream(service.list().spliterator(), false)
                .map(this::toRokDTO)
                .collect(Collectors.toList()); // Java 11 OK
    }

    // ✅ DTO - nemoj vraćati entity (one() ti je pravio lazy problem)
    @GetMapping("/{id}")
    public IspitniRokDTO one(@PathVariable Long id) {
        IspitniRok r = service.get(id);
        return toRokDTO(r);
    }

    // ✅ DTO lista ispita - nema lazy serijalizacije (ne vraćamo Ispit entity)
    @GetMapping("/{id}/ispiti")
    public List<IspitDTO> ispiti(@PathVariable Long id) {
        IspitniRok rok = service.get(id);

        return StreamSupport.stream(ispitRepo.findAll().spliterator(), false)
                .filter(i -> i.getIspitniRok() != null && i.getIspitniRok().getId().equals(rok.getId()))
                .map(i -> {
                    IspitDTO dto = new IspitDTO();
                    dto.setId(i.getId());
                    dto.setDatumOdrzavanja(i.getDatumOdrzavanja());
                    dto.setVremePocetka(i.getVremePocetka());
                    dto.setZakljucen(i.isZakljucen());

                    if (i.getDrziPredmet() != null) {
                        dto.setDrziPredmetId(i.getDrziPredmet().getId());
                        if (i.getDrziPredmet().getPredmet() != null)
                            dto.setPredmetNaziv(i.getDrziPredmet().getPredmet().getNaziv());
                        if (i.getDrziPredmet().getNastavnik() != null)
                            dto.setNastavnikImePrezime(
                                    i.getDrziPredmet().getNastavnik().getIme() + " " +
                                            i.getDrziPredmet().getNastavnik().getPrezime()
                            );
                    }
                    return dto;
                })
                .collect(java.util.stream.Collectors.toList());
    }


    private IspitniRokDTO toRokDTO(IspitniRok r) {
        IspitniRokDTO dto = new IspitniRokDTO();
        dto.setId(r.getId());
        dto.setDatumPocetka(r.getDatumPocetka());
        dto.setDatumZavrsetka(r.getDatumZavrsetka());
        dto.setSkolskaGodinaId(r.getSkolskaGodina() == null ? null : r.getSkolskaGodina().getId());
        dto.setRegistrationStart(r.getRegistrationStart());
        dto.setRegistrationEnd(r.getRegistrationEnd());
        dto.setCancellationEnd(r.getCancellationEnd());
        dto.setActive(r.isActive());
        dto.setExamCount(ispitRepo.findByIspitniRokId(r.getId()).size());
        dto.setReady(r.isActive() && r.getRegistrationStart() != null && r.getRegistrationEnd() != null
                && r.getCancellationEnd() != null && dto.getExamCount() > 0);
        return dto;
    }

    private IspitDTO toIspitDTO(Ispit i) {
        IspitDTO dto = new IspitDTO();
        dto.setId(i.getId());
        dto.setDatumOdrzavanja(i.getDatumOdrzavanja());
        dto.setVremePocetka(i.getVremePocetka());
        dto.setZakljucen(i.isZakljucen());
        return dto;
    }
}
