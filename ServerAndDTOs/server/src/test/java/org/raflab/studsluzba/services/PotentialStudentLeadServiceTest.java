package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.PotentialStudentLead;
import org.raflab.studsluzba.model.LeadExportLog;
import org.raflab.studsluzba.model.dtos.LeadDTO;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.PotentialStudentLeadRepository;
import org.raflab.studsluzba.repositories.LeadExportLogRepository;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.mockito.ArgumentCaptor;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.eq;

class PotentialStudentLeadServiceTest {
    @Test
    void listMasksPersonalDataForRegularAdmin() {
        PotentialStudentLeadRepository repo = mock(PotentialStudentLeadRepository.class);
        PermissionService permissions = mock(PermissionService.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        PotentialStudentLead lead = lead();
        when(currentUser.isHeadAdmin()).thenReturn(false);
        when(repo.findAllByOrderByCreatedAtDesc(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(lead)));

        PotentialStudentLeadService service = service(repo, permissions, currentUser);
        LeadDTO dto = service.list(0, 20, "127.0.0.1", "test").getContent().get(0);

        verify(permissions).require(Permission.LEADS_READ);
        assertThat(dto.isFullAccess()).isFalse();
        assertThat(dto.getMaskedName()).isEqualTo("M**** P****");
        assertThat(dto.getMaskedEmail()).isEqualTo("m****@****.***");
        assertThat(dto.getFirstName()).isNull();
        assertThat(dto.getLastName()).isNull();
        assertThat(dto.getFullName()).isNull();
        assertThat(dto.getEmail()).isNull();
        assertThat(dto.getPhone()).isNull();
        assertThat(dto.getInterestedProgram()).isNull();
        assertThat(dto.getSource()).isNull();
        assertThat(dto.getNote()).isNull();
    }

    @Test
    void exportMasksPersonalDataForRegularAdmin() {
        PotentialStudentLeadRepository repo = mock(PotentialStudentLeadRepository.class);
        PermissionService permissions = mock(PermissionService.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        when(currentUser.isHeadAdmin()).thenReturn(false);
        when(repo.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(lead()));

        PotentialStudentLeadService service = service(repo, permissions, currentUser);
        String csv = new String(service.exportCsv("127.0.0.1", "test", null), StandardCharsets.UTF_8);

        verify(permissions).require(Permission.LEADS_EXPORT);
        assertThat(csv).contains("masked_name,masked_email,status");
        assertThat(csv).contains("\"M**** P****\",\"m****@****.***\",\"NEW\"");
        assertThat(csv).doesNotContain("Marko", "Petrovic", "marko.petrovic@example.test", "+38160123456", "Softversko inzenjerstvo", "summer-campaign", "Sensitive note");
    }

    @Test
    void exportIncludesPersonalDataForHeadAdmin() {
        PotentialStudentLeadRepository repo = mock(PotentialStudentLeadRepository.class);
        PermissionService permissions = mock(PermissionService.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        when(currentUser.isHeadAdmin()).thenReturn(true);
        when(repo.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(lead()));

        PotentialStudentLeadService service = service(repo, permissions, currentUser);
        String csv = new String(service.exportCsv("127.0.0.1", "test", null), StandardCharsets.UTF_8);

        assertThat(csv).contains("id,first_name,last_name,full_name,email,phone,interested_program,source,note,status,created_at");
        assertThat(csv).contains("Marko", "Petrovic", "marko.petrovic@example.test", "+38160123456", "Sensitive note");
    }

    @Test
    void exportCreatesMaskedExportAndAuditLogsForAdmin() {
        PotentialStudentLeadRepository repo = mock(PotentialStudentLeadRepository.class);
        PermissionService permissions = mock(PermissionService.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        LeadAuditService audit = mock(LeadAuditService.class);
        LeadExportLogRepository exportLogs = mock(LeadExportLogRepository.class);
        UserAccount admin = new UserAccount();
        admin.setId(9L);
        admin.setUsername("admin@example.test");
        admin.setRole(Role.ADMIN);
        when(currentUser.isHeadAdmin()).thenReturn(false);
        when(currentUser.account()).thenReturn(admin);
        when(currentUser.role()).thenReturn(Role.ADMIN);
        when(repo.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(lead()));

        PotentialStudentLeadService service = new PotentialStudentLeadService(
                repo, permissions, currentUser, audit, exportLogs);
        service.exportCsv("127.0.0.1", "test-agent", "status=NEW");

        ArgumentCaptor<LeadExportLog> capture = ArgumentCaptor.forClass(LeadExportLog.class);
        verify(exportLogs).save(capture.capture());
        assertThat(capture.getValue().isMasked()).isTrue();
        assertThat(capture.getValue().getRecordCount()).isEqualTo(1);
        assertThat(capture.getValue().getExporterRole()).isEqualTo(Role.ADMIN);
        assertThat(capture.getValue().getFilters()).isEqualTo("status=NEW");
        verify(audit).log(eq(null), eq("LEAD_CSV_EXPORTED"), any(), eq(null), eq("MASKED"),
                eq("127.0.0.1"), eq("test-agent"));
    }

    private PotentialStudentLead lead() {
        PotentialStudentLead lead = new PotentialStudentLead();
        lead.setId(42L);
        lead.setFirstName("Marko");
        lead.setLastName("Petrovic");
        lead.setEmail("marko.petrovic@example.test");
        lead.setPhone("+38160123456");
        lead.setInterestedProgram("Softversko inzenjerstvo");
        lead.setSource("summer-campaign");
        lead.setNote("Sensitive note");
        lead.setStatus(org.raflab.studsluzba.model.LeadStatus.NEW);
        lead.setCreatedAt(LocalDateTime.of(2026, 6, 23, 12, 30));
        return lead;
    }

    private PotentialStudentLeadService service(PotentialStudentLeadRepository repo,
                                                PermissionService permissions,
                                                CurrentUser currentUser) {
        return new PotentialStudentLeadService(repo, permissions, currentUser,
                mock(LeadAuditService.class), mock(LeadExportLogRepository.class));
    }
}
