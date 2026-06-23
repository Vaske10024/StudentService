package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.PotentialStudentLead;
import org.raflab.studsluzba.model.dtos.LeadDTO;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.repositories.PotentialStudentLeadRepository;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        PotentialStudentLeadService service = new PotentialStudentLeadService(repo, permissions, currentUser);
        LeadDTO dto = service.list(0, 20).getContent().get(0);

        verify(permissions).require(Permission.LEADS_READ);
        assertThat(dto.isFullAccess()).isFalse();
        assertThat(dto.getInitials()).isEqualTo("MP");
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

        PotentialStudentLeadService service = new PotentialStudentLeadService(repo, permissions, currentUser);
        String csv = new String(service.exportCsv(), StandardCharsets.UTF_8);

        verify(permissions).require(Permission.LEADS_EXPORT);
        assertThat(csv).contains("initials");
        assertThat(csv).contains("\"MP\"");
        assertThat(csv).doesNotContain("Marko", "Petrovic", "marko.petrovic@example.test", "+38160123456", "Softversko inzenjerstvo", "summer-campaign", "Sensitive note");
    }

    @Test
    void exportIncludesPersonalDataForHeadAdmin() {
        PotentialStudentLeadRepository repo = mock(PotentialStudentLeadRepository.class);
        PermissionService permissions = mock(PermissionService.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        when(currentUser.isHeadAdmin()).thenReturn(true);
        when(repo.findAllByOrderByCreatedAtDesc()).thenReturn(Collections.singletonList(lead()));

        PotentialStudentLeadService service = new PotentialStudentLeadService(repo, permissions, currentUser);
        String csv = new String(service.exportCsv(), StandardCharsets.UTF_8);

        assertThat(csv).contains("id,first_name,last_name,full_name,email,phone,interested_program,source,note,created_at");
        assertThat(csv).contains("Marko", "Petrovic", "marko.petrovic@example.test", "+38160123456", "Sensitive note");
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
        lead.setCreatedAt(LocalDateTime.of(2026, 6, 23, 12, 30));
        return lead;
    }
}
