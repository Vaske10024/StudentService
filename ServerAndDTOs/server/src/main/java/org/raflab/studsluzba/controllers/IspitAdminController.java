package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.IspitCreateRequest;
import org.raflab.studsluzba.model.dtos.IspitUpdateTimeRequest;
import org.raflab.studsluzba.services.IspitAdminService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ispit/admin")
@RequiredArgsConstructor
@Validated
public class IspitAdminController {

    private final IspitAdminService service;

    @PostMapping("/create")
    public Long create(@RequestBody @Validated IspitCreateRequest req) {
        return service.create(req.getRokId(), req.getDrziPredmetId(), req.getDatum(), req.getVreme());
    }

    @PatchMapping("/{id}/vreme")
    public void updateTime(@PathVariable Long id, @RequestBody IspitUpdateTimeRequest req) {
        service.updateTime(id, req.getDatum(), req.getVreme());
    }

    @PatchMapping("/{id}/zakljucaj")
    public void lock(@PathVariable Long id) {
        service.lock(id);
    }
}
