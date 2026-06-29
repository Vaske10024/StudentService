package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.LeadEmailTemplateDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailTemplateRequest;
import org.raflab.studsluzba.services.LeadEmailTemplateService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leads/admin/templates")
@RequiredArgsConstructor
public class LeadEmailTemplateController {
    private final LeadEmailTemplateService service;

    @GetMapping
    public List<LeadEmailTemplateDTO> list() {
        return service.listAvailable();
    }

    @PostMapping
    public LeadEmailTemplateDTO create(@RequestBody @Valid LeadEmailTemplateRequest request) {
        return service.create(request);
    }

    @PutMapping("/{id}")
    public LeadEmailTemplateDTO update(@PathVariable Long id,
                                       @RequestBody @Valid LeadEmailTemplateRequest request) {
        return service.update(id, request);
    }
}
