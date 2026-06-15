package org.raflab.studsluzba.controllers;


import java.util.List;

import org.raflab.studsluzba.model.dtos.NastavnikLiteDTO;
import org.raflab.studsluzba.model.dtos.NastavnikRequest;
import org.raflab.studsluzba.model.dtos.NastavnikResponse;
import org.raflab.studsluzba.model.dtos.ProfessorProvisionDTO;
import org.raflab.studsluzba.utils.Converters;

import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.services.NastavnikService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
@RequestMapping(path = "/api/nastavnik")
public class NastavnikController {

    @Autowired
    NastavnikService nastavnikService;

    /*
        Nastanik info za test:
        benji@gmail.com
        id:16


     */
    @PostMapping(path = "/add")
    public Long addNewNastavnik(@RequestBody @Valid NastavnikRequest nastavnikRequest) {
        Nastavnik nastavnik = nastavnikService.save(Converters.toNastavnik(nastavnikRequest));
        return nastavnik.getId();
    }

    @PostMapping(path = "/{id}/provision-account")
    public ProfessorProvisionDTO provisionAccount(@PathVariable Long id) {
        return nastavnikService.provisionAccount(id);
    }

    @GetMapping("/all")
    public List<NastavnikLiteDTO> all() {
        return nastavnikService.findAllLite();
    }

    @GetMapping(path = "/{id}")
    public NastavnikResponse getNastavnikById(@PathVariable Long id)
    {
        return nastavnikService.details(id);
    }

    @GetMapping("/search")
    public List<NastavnikLiteDTO> search(@RequestParam(required=false) String ime,
                                         @RequestParam(required=false) String prezime) {
        return nastavnikService.searchLite(ime, prezime);
    }

}
