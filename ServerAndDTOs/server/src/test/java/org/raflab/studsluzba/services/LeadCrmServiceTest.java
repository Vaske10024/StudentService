package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.raflab.studsluzba.model.LeadEmailMessage;
import org.raflab.studsluzba.model.LeadStatus;
import org.raflab.studsluzba.model.PotentialStudentLead;
import org.raflab.studsluzba.model.dtos.LeadEmailMessageDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailSendRequest;
import org.raflab.studsluzba.model.dtos.LeadDTO;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.LeadEmailMessageRepository;
import org.raflab.studsluzba.repositories.LeadEmailTemplateRepository;
import org.raflab.studsluzba.repositories.PotentialStudentLeadRepository;
import org.raflab.studsluzba.security.CurrentUser;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LeadCrmServiceTest {
    @Test
    void regularAdminDetailRemainsMasked() {
        PotentialStudentLeadRepository leads = mock(PotentialStudentLeadRepository.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        LeadAuditService audit = mock(LeadAuditService.class);
        PotentialStudentLead lead = lead();
        when(leads.findById(42L)).thenReturn(Optional.of(lead));
        when(currentUser.isHeadAdmin()).thenReturn(false);

        PotentialStudentLeadService mapper = new PotentialStudentLeadService(leads,
                mock(PermissionService.class), currentUser, audit,
                mock(org.raflab.studsluzba.repositories.LeadExportLogRepository.class));
        LeadCrmService service = new LeadCrmService(leads, mock(LeadEmailMessageRepository.class),
                mock(LeadEmailTemplateRepository.class), mapper, mock(MailService.class), currentUser, audit);

        LeadDTO result = service.detail(42L, "127.0.0.1", "test-agent");

        assertThat(result.isFullAccess()).isFalse();
        assertThat(result.getMaskedEmail()).isEqualTo("r****@****.***");
        assertThat(result.getEmail()).isNull();
        assertThat(result.getFirstName()).isNull();
        assertThat(result.getLastName()).isNull();
        assertThat(result.getPhone()).isNull();
        assertThat(result.getNote()).isNull();
    }

    @Test
    void adminSendsByLeadIdAndPersistsExactSnapshotsWithoutRecipientInResponse() {
        PotentialStudentLeadRepository leads = mock(PotentialStudentLeadRepository.class);
        LeadEmailMessageRepository messages = mock(LeadEmailMessageRepository.class);
        LeadEmailTemplateRepository templates = mock(LeadEmailTemplateRepository.class);
        PotentialStudentLeadService leadService = mock(PotentialStudentLeadService.class);
        MailService mail = mock(MailService.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        LeadAuditService audit = mock(LeadAuditService.class);
        PotentialStudentLead lead = lead();
        UserAccount admin = admin();

        when(leads.findById(42L)).thenReturn(Optional.of(lead));
        when(currentUser.account()).thenReturn(admin);
        when(mail.send(any(), any(), any())).thenReturn(MailSendResult.sent("provider-123"));
        when(messages.save(any())).thenAnswer(invocation -> {
            LeadEmailMessage saved = invocation.getArgument(0);
            saved.setId(7L);
            saved.setCreatedAt(LocalDateTime.of(2026, 6, 25, 12, 0));
            return saved;
        });

        LeadCrmService service = new LeadCrmService(leads, messages, templates, leadService,
                mail, currentUser, audit);
        String subject = "  Welcome to our program  ";
        String body = "Hello,\n\nExact body with trailing spaces.  ";
        LeadEmailMessageDTO result = service.send(42L,
                new LeadEmailSendRequest(null, subject, body), "127.0.0.1", "test-agent");

        verify(mail).send("real.lead@example.test", subject, body);
        ArgumentCaptor<LeadEmailMessage> capture = ArgumentCaptor.forClass(LeadEmailMessage.class);
        verify(messages).save(capture.capture());
        assertThat(capture.getValue().getSubjectSnapshot()).isEqualTo(subject);
        assertThat(capture.getValue().getBodySnapshot()).isEqualTo(body);
        assertThat(capture.getValue().getStatus()).isEqualTo(LeadEmailMessage.Status.SENT);
        assertThat(result.getSubject()).isEqualTo(subject);
        assertThat(result.getBody()).isEqualTo(body);
        assertThat(result.toString()).doesNotContain("real.lead@example.test");
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.CONTACTED);
        verify(audit).log(eq(lead), eq("LEAD_EMAIL_SENT"), any(), eq(null), eq("SENT"),
                eq("127.0.0.1"), eq("test-agent"));
        verify(audit).log(eq(lead), eq("LEAD_STATUS_CHANGED"), any(), eq("NEW"), eq("CONTACTED"),
                eq("127.0.0.1"), eq("test-agent"));
    }

    @Test
    void failedSendIsPersistedAndDoesNotChangeLeadStatus() {
        PotentialStudentLeadRepository leads = mock(PotentialStudentLeadRepository.class);
        LeadEmailMessageRepository messages = mock(LeadEmailMessageRepository.class);
        MailService mail = mock(MailService.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        PotentialStudentLead lead = lead();
        when(leads.findById(42L)).thenReturn(Optional.of(lead));
        when(currentUser.account()).thenReturn(admin());
        when(mail.send(any(), any(), any())).thenReturn(MailSendResult.failed("SMTP is not configured."));
        when(messages.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LeadCrmService service = new LeadCrmService(leads, messages,
                mock(LeadEmailTemplateRepository.class), mock(PotentialStudentLeadService.class),
                mail, currentUser, mock(LeadAuditService.class));
        LeadEmailMessageDTO result = service.send(42L,
                new LeadEmailSendRequest(null, "Subject", "Body"), null, null);

        assertThat(result.getStatus()).isEqualTo("FAILED");
        assertThat(result.getErrorMessage()).isEqualTo("SMTP is not configured.");
        assertThat(lead.getStatus()).isEqualTo(LeadStatus.NEW);
    }

    private PotentialStudentLead lead() {
        PotentialStudentLead lead = new PotentialStudentLead();
        lead.setId(42L);
        lead.setFirstName("Real");
        lead.setLastName("Lead");
        lead.setEmail("real.lead@example.test");
        lead.setStatus(LeadStatus.NEW);
        return lead;
    }

    private UserAccount admin() {
        UserAccount user = new UserAccount();
        user.setId(9L);
        user.setUsername("admin@example.test");
        user.setRole(Role.ADMIN);
        return user;
    }
}
