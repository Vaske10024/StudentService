package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.AuditLogDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailMonitoringDTO;
import org.raflab.studsluzba.model.dtos.LeadExportLogDTO;
import org.raflab.studsluzba.services.LeadMonitoringService;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leads/admin/monitoring")
@RequiredArgsConstructor
@PreAuthorize("hasRole('HEAD_ADMIN')")
public class LeadMonitoringController {
    private final LeadMonitoringService service;

    @GetMapping("/emails")
    public Page<LeadEmailMonitoringDTO> emails(@RequestParam(defaultValue = "0") int page,
                                               @RequestParam(defaultValue = "50") int size) {
        return service.emails(page, size);
    }

    @GetMapping("/audit")
    public Page<AuditLogDTO> audits(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "50") int size) {
        return service.audits(page, size);
    }

    @GetMapping("/exports")
    public Page<LeadExportLogDTO> exports(@RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "50") int size) {
        return service.exports(page, size);
    }
}
