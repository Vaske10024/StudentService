package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.PotentialStudentLead;
import org.raflab.studsluzba.model.LeadExportLog;
import org.raflab.studsluzba.model.LeadStatus;
import org.raflab.studsluzba.model.dtos.LeadCreateRequest;
import org.raflab.studsluzba.model.dtos.LeadDTO;
import org.raflab.studsluzba.model.dtos.LeadSubmissionResponse;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.repositories.PotentialStudentLeadRepository;
import org.raflab.studsluzba.repositories.LeadExportLogRepository;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class PotentialStudentLeadService {
    private final PotentialStudentLeadRepository leadRepository;
    private final PermissionService permissions;
    private final CurrentUser currentUser;
    private final LeadAuditService auditService;
    private final LeadExportLogRepository exportLogRepository;

    @Transactional
    public LeadSubmissionResponse create(LeadCreateRequest request, String remoteAddress, String userAgent) {
        PotentialStudentLead lead = new PotentialStudentLead();
        lead.setFirstName(required(request.getFirstName()));
        lead.setLastName(required(request.getLastName()));
        lead.setEmail(required(request.getEmail()).toLowerCase(Locale.ROOT));
        lead.setPhone(optional(request.getPhone()));
        lead.setInterestedProgram(optional(request.getInterestedProgram()));
        lead.setSource(optional(request.getSource()));
        lead.setNote(optional(request.getNote()));
        lead.setPrivacyConsent(request.isPrivacyConsent());
        lead.setStatus(LeadStatus.NEW);
        lead.setConsentAt(LocalDateTime.now());
        lead.setRemoteAddress(limit(optional(remoteAddress), 64));
        lead.setUserAgent(limit(optional(userAgent), 255));
        leadRepository.save(lead);
        auditService.log(lead, "LEAD_CREATED", "Public lead submitted.", null,
                LeadStatus.NEW.name(), remoteAddress, userAgent);
        return new LeadSubmissionResponse("Hvala. Uskoro cemo vas kontaktirati.");
    }

    @Transactional
    public Page<LeadDTO> list(int page, int size, String ipAddress, String userAgent) {
        permissions.require(Permission.LEADS_READ);
        int safeSize = Math.max(1, Math.min(size, 100));
        boolean fullAccess = currentUser.isHeadAdmin();
        Page<LeadDTO> result = leadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0, page), safeSize))
                .map(lead -> toDto(lead, fullAccess));
        auditService.log(null, fullAccess ? "LEAD_FULL_DATA_VIEWED" : "LEAD_VIEWED",
                fullAccess ? "HEAD_ADMIN viewed a page of full lead data." : "ADMIN viewed a page of masked lead data.",
                null, null, ipAddress, userAgent);
        return result;
    }

    @Transactional
    public byte[] exportCsv(String ipAddress, String userAgent, String filters) {
        permissions.require(Permission.LEADS_EXPORT);
        boolean fullAccess = currentUser.isHeadAdmin();
        java.util.List<PotentialStudentLead> leads = leadRepository.findAllByOrderByCreatedAtDesc();
        StringBuilder csv = new StringBuilder();
        if (fullAccess) {
            csv.append("id,first_name,last_name,full_name,email,phone,interested_program,source,note,status,created_at\n");
        } else {
            csv.append("masked_name,masked_email,status\n");
        }
        for (PotentialStudentLead lead : leads) {
            if (fullAccess) {
                csv.append(lead.getId()).append(',')
                        .append(csv(lead.getFirstName())).append(',')
                        .append(csv(lead.getLastName())).append(',')
                        .append(csv(fullName(lead))).append(',')
                        .append(csv(lead.getEmail())).append(',')
                        .append(csv(lead.getPhone())).append(',')
                        .append(csv(lead.getInterestedProgram())).append(',')
                        .append(csv(lead.getSource())).append(',')
                        .append(csv(lead.getNote())).append(',')
                        .append(csv(lead.getStatus() == null ? LeadStatus.NEW.name() : lead.getStatus().name())).append(',')
                        .append(csv(value(lead.getCreatedAt())))
                        .append('\n');
            } else {
                csv.append(csv(maskedName(lead))).append(',')
                        .append(csv(maskedEmail(lead.getEmail()))).append(',')
                        .append(csv(lead.getStatus() == null ? LeadStatus.NEW.name() : lead.getStatus().name()))
                        .append('\n');
            }
        }
        LeadExportLog export = new LeadExportLog();
        export.setExportedBy(currentUser.account());
        export.setExporterRole(currentUser.role());
        export.setExportType("LEADS_CSV");
        export.setMasked(!fullAccess);
        export.setRecordCount(leads.size());
        export.setFilters(limit(optional(filters), 1000));
        export.setIpAddress(limit(optional(ipAddress), 64));
        export.setUserAgent(limit(optional(userAgent), 255));
        exportLogRepository.save(export);
        auditService.log(null, "LEAD_CSV_EXPORTED",
                "Lead CSV exported; masked=" + (!fullAccess) + ", records=" + leads.size(),
                null, fullAccess ? "FULL" : "MASKED", ipAddress, userAgent);
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    LeadDTO toDto(PotentialStudentLead lead, boolean fullAccess) {
        LeadDTO dto = new LeadDTO();
        dto.setId(lead.getId());
        dto.setCreatedAt(lead.getCreatedAt());
        dto.setStatus(lead.getStatus() == null ? LeadStatus.NEW.name() : lead.getStatus().name());
        dto.setFullAccess(fullAccess);
        if (fullAccess) {
            dto.setFirstName(lead.getFirstName());
            dto.setLastName(lead.getLastName());
            dto.setFullName(fullName(lead));
            dto.setEmail(lead.getEmail());
            dto.setPhone(lead.getPhone());
            dto.setInterestedProgram(lead.getInterestedProgram());
            dto.setSource(lead.getSource());
            dto.setNote(lead.getNote());
        } else {
            dto.setMaskedName(maskedName(lead));
            dto.setMaskedEmail(maskedEmail(lead.getEmail()));
        }
        return dto;
    }

    private String maskedName(PotentialStudentLead lead) {
        return maskNamePart(lead.getFirstName()) + " " + maskNamePart(lead.getLastName());
    }

    private String maskNamePart(String value) {
        String cleaned = optional(value);
        return cleaned == null ? "" : cleaned.substring(0, 1).toUpperCase(Locale.ROOT) + "****";
    }

    private String maskedEmail(String email) {
        String cleaned = optional(email);
        if (cleaned == null) {
            return "";
        }
        return cleaned.substring(0, 1).toLowerCase(Locale.ROOT) + "****@****.***";
    }

    private String fullName(PotentialStudentLead lead) {
        return (lead.getFirstName() + " " + lead.getLastName()).trim();
    }

    private String required(String value) {
        String cleaned = optional(value);
        if (cleaned == null) {
            throw new IllegalArgumentException("Obavezno polje nije popunjeno.");
        }
        return cleaned;
    }

    private String optional(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    private String limit(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }

    private String csv(String value) {
        return value == null ? "" : "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }
}
