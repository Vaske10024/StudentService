package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.LeadCreateRequest;
import org.raflab.studsluzba.model.dtos.LeadDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailMessageDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailSendRequest;
import org.raflab.studsluzba.model.dtos.LeadStatusUpdateRequest;
import org.raflab.studsluzba.model.dtos.LeadSubmissionResponse;
import org.raflab.studsluzba.services.LeadCrmService;
import org.raflab.studsluzba.services.PotentialStudentLeadService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Validated
public class PotentialStudentLeadController {
    private final PotentialStudentLeadService service;
    private final LeadCrmService crmService;

    @PostMapping
    public LeadSubmissionResponse create(@RequestBody @Valid LeadCreateRequest request,
                                         HttpServletRequest httpRequest) {
        return service.create(request, clientAddress(httpRequest), httpRequest.getHeader("User-Agent"));
    }

    @GetMapping("/admin")
    public Page<LeadDTO> list(@RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int size,
                              HttpServletRequest request) {
        return service.list(page, size, clientAddress(request), request.getHeader("User-Agent"));
    }

    @GetMapping(value = "/admin/export.csv", produces = "text/csv")
    public ResponseEntity<byte[]> export(HttpServletRequest request) {
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"potential-student-leads.csv\"")
                .contentType(new MediaType("text", "csv"))
                .body(service.exportCsv(clientAddress(request), request.getHeader("User-Agent"),
                        request.getQueryString()));
    }

    @GetMapping("/admin/{leadId}")
    public LeadDTO detail(@PathVariable Long leadId, HttpServletRequest request) {
        return crmService.detail(leadId, clientAddress(request), request.getHeader("User-Agent"));
    }

    @PatchMapping("/admin/{leadId}/status")
    public LeadDTO updateStatus(@PathVariable Long leadId,
                                @RequestBody @Valid LeadStatusUpdateRequest status,
                                HttpServletRequest request) {
        return crmService.updateStatus(leadId, status, clientAddress(request), request.getHeader("User-Agent"));
    }

    @PostMapping("/admin/{leadId}/emails")
    public LeadEmailMessageDTO sendEmail(@PathVariable Long leadId,
                                         @RequestBody @Valid LeadEmailSendRequest email,
                                         HttpServletRequest request) {
        return crmService.send(leadId, email, clientAddress(request), request.getHeader("User-Agent"));
    }

    @GetMapping("/admin/{leadId}/emails")
    public List<LeadEmailMessageDTO> history(@PathVariable Long leadId) {
        return crmService.history(leadId);
    }

    private String clientAddress(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.trim().isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
