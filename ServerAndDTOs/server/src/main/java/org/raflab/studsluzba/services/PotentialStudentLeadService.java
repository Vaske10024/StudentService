package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.PotentialStudentLead;
import org.raflab.studsluzba.model.dtos.LeadCreateRequest;
import org.raflab.studsluzba.model.dtos.LeadDTO;
import org.raflab.studsluzba.model.dtos.LeadSubmissionResponse;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.repositories.PotentialStudentLeadRepository;
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
        lead.setConsentAt(LocalDateTime.now());
        lead.setRemoteAddress(limit(optional(remoteAddress), 64));
        lead.setUserAgent(limit(optional(userAgent), 255));
        leadRepository.save(lead);
        return new LeadSubmissionResponse("Hvala. Uskoro cemo vas kontaktirati.");
    }

    @Transactional(readOnly = true)
    public Page<LeadDTO> list(int page, int size) {
        permissions.require(Permission.LEADS_READ);
        int safeSize = Math.max(1, Math.min(size, 100));
        boolean fullAccess = currentUser.isHeadAdmin();
        return leadRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(Math.max(0, page), safeSize))
                .map(lead -> toDto(lead, fullAccess));
    }

    @Transactional(readOnly = true)
    public byte[] exportCsv() {
        permissions.require(Permission.LEADS_EXPORT);
        boolean fullAccess = currentUser.isHeadAdmin();
        StringBuilder csv = new StringBuilder();
        if (fullAccess) {
            csv.append("id,first_name,last_name,full_name,email,phone,interested_program,source,note,created_at\n");
        } else {
            csv.append("initials\n");
        }
        for (PotentialStudentLead lead : leadRepository.findAllByOrderByCreatedAtDesc()) {
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
                        .append(csv(value(lead.getCreatedAt())))
                        .append('\n');
            } else {
                csv.append(csv(initials(lead)))
                        .append('\n');
            }
        }
        return csv.toString().getBytes(StandardCharsets.UTF_8);
    }

    LeadDTO toDto(PotentialStudentLead lead, boolean fullAccess) {
        LeadDTO dto = new LeadDTO();
        dto.setId(lead.getId());
        dto.setInitials(initials(lead));
        dto.setCreatedAt(lead.getCreatedAt());
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
        }
        return dto;
    }

    private String initials(PotentialStudentLead lead) {
        return initial(lead.getFirstName()) + initial(lead.getLastName());
    }

    private String initial(String value) {
        String cleaned = optional(value);
        return cleaned == null ? "" : cleaned.substring(0, 1).toUpperCase(Locale.ROOT);
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
