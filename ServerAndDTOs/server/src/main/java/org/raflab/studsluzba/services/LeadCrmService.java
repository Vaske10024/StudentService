package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.LeadEmailMessage;
import org.raflab.studsluzba.model.LeadEmailTemplate;
import org.raflab.studsluzba.model.LeadStatus;
import org.raflab.studsluzba.model.PotentialStudentLead;
import org.raflab.studsluzba.model.dtos.LeadDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailMessageDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailSendRequest;
import org.raflab.studsluzba.model.dtos.LeadStatusUpdateRequest;
import org.raflab.studsluzba.repositories.LeadEmailMessageRepository;
import org.raflab.studsluzba.repositories.LeadEmailTemplateRepository;
import org.raflab.studsluzba.repositories.PotentialStudentLeadRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadCrmService {
    private final PotentialStudentLeadRepository leadRepository;
    private final LeadEmailMessageRepository messageRepository;
    private final LeadEmailTemplateRepository templateRepository;
    private final PotentialStudentLeadService leadService;
    private final MailService mailService;
    private final CurrentUser currentUser;
    private final LeadAuditService auditService;

    @Transactional
    public LeadDTO detail(Long leadId, String ipAddress, String userAgent) {
        currentUser.requireAdmin();
        PotentialStudentLead lead = lead(leadId);
        boolean fullAccess = currentUser.isHeadAdmin();
        auditService.log(lead, "LEAD_VIEWED", "Lead detail viewed.", null, null, ipAddress, userAgent);
        if (fullAccess) {
            auditService.log(lead, "LEAD_FULL_DATA_VIEWED", "Full lead data viewed by HEAD_ADMIN.",
                    null, null, ipAddress, userAgent);
        }
        return leadService.toDto(lead, fullAccess);
    }

    @Transactional
    public LeadDTO updateStatus(Long leadId, LeadStatusUpdateRequest request, String ipAddress, String userAgent) {
        currentUser.requireAdmin();
        PotentialStudentLead lead = lead(leadId);
        LeadStatus next;
        try {
            next = LeadStatus.valueOf(request.getStatus().trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Unsupported lead status.");
        }
        LeadStatus previous = lead.getStatus();
        lead.setStatus(next);
        leadRepository.save(lead);
        auditService.log(lead, "LEAD_STATUS_CHANGED", "Lead status changed manually.",
                previous == null ? null : previous.name(), next.name(), ipAddress, userAgent);
        return leadService.toDto(lead, currentUser.isHeadAdmin());
    }

    @Transactional
    public LeadEmailMessageDTO send(Long leadId, LeadEmailSendRequest request,
                                    String ipAddress, String userAgent) {
        currentUser.requireAdmin();
        if (request.getSubject().trim().isEmpty() || request.getBody().trim().isEmpty()) {
            throw ApiException.badRequest("Subject and message body are required.");
        }
        PotentialStudentLead lead = lead(leadId);
        LeadEmailTemplate template = null;
        if (request.getTemplateId() != null) {
            template = templateRepository.findById(request.getTemplateId())
                    .orElseThrow(() -> ApiException.notFound("Email template does not exist."));
            if (!template.isActive()) {
                throw ApiException.badRequest("The selected email template is inactive.");
            }
        }

        MailSendResult result = mailService.send(lead.getEmail(), request.getSubject(), request.getBody());
        LeadEmailMessage message = new LeadEmailMessage();
        message.setLead(lead);
        message.setSentBy(currentUser.account());
        message.setTemplate(template);
        message.setSubjectSnapshot(request.getSubject());
        message.setBodySnapshot(request.getBody());
        message.setStatus(result.isSent() ? LeadEmailMessage.Status.SENT : LeadEmailMessage.Status.FAILED);
        message.setProviderMessageId(result.getProviderMessageId());
        message.setErrorMessage(result.getErrorMessage());
        if (result.isSent()) {
            message.setSentAt(LocalDateTime.now());
        }
        message = messageRepository.save(message);

        String action = result.isSent() ? "LEAD_EMAIL_SENT" : "LEAD_EMAIL_FAILED";
        String description = "Lead email " + (result.isSent() ? "sent." : "failed.")
                + (template == null ? "" : " Template id=" + template.getId() + ".");
        auditService.log(lead, action, description, null, message.getStatus().name(), ipAddress, userAgent);

        if (result.isSent() && lead.getStatus() == LeadStatus.NEW) {
            lead.setStatus(LeadStatus.CONTACTED);
            leadRepository.save(lead);
            auditService.log(lead, "LEAD_STATUS_CHANGED", "Lead marked CONTACTED after successful email.",
                    LeadStatus.NEW.name(), LeadStatus.CONTACTED.name(), ipAddress, userAgent);
        }
        return toDto(message);
    }

    @Transactional(readOnly = true)
    public List<LeadEmailMessageDTO> history(Long leadId) {
        currentUser.requireAdmin();
        lead(leadId);
        return messageRepository.findByLeadIdOrderByCreatedAtDesc(leadId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    LeadEmailMessageDTO toDto(LeadEmailMessage message) {
        return new LeadEmailMessageDTO(
                message.getId(),
                message.getLead() == null ? null : message.getLead().getId(),
                message.getTemplate() == null ? null : message.getTemplate().getId(),
                message.getTemplate() == null ? null : message.getTemplate().getName(),
                message.getSentBy() == null ? null : message.getSentBy().getId(),
                message.getSentBy() == null ? null : message.getSentBy().getUsername(),
                message.getSubjectSnapshot(),
                message.getBodySnapshot(),
                message.getStatus() == null ? null : message.getStatus().name(),
                message.getProviderMessageId(),
                message.getErrorMessage(),
                message.getSentAt(),
                message.getCreatedAt()
        );
    }

    private PotentialStudentLead lead(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Lead does not exist."));
    }
}
