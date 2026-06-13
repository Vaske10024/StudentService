package org.raflab.studsluzba.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentStatus;
import org.raflab.studsluzba.model.StudentStatusHistory;
import org.raflab.studsluzba.model.dtos.StudentStatusChangeRequest;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.StudentStatusHistoryRepository;
import org.raflab.studsluzba.repositories.StudentStatusRequestRepository;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class StudentLifecycleServiceTest {
    private StudentIndeksRepository indeksRepo;
    private StudentStatusHistoryRepository historyRepo;
    private AuditLogRepository auditRepo;
    private CurrentUser currentUser;
    private StudentLifecycleService service;

    @BeforeEach
    void setUp() {
        indeksRepo = mock(StudentIndeksRepository.class);
        historyRepo = mock(StudentStatusHistoryRepository.class);
        auditRepo = mock(AuditLogRepository.class);
        currentUser = mock(CurrentUser.class);
        service = new StudentLifecycleService(indeksRepo, historyRepo, mock(StudentStatusRequestRepository.class), auditRepo, currentUser);
        when(currentUser.userId()).thenReturn(7L);
    }

    @Test
    void activeToDormantSynchronizesLegacyFlagAndWritesHistoryAndAudit() {
        StudentIndeks indeks = indeks(StudentStatus.AKTIVAN, true);
        when(indeksRepo.findById(1L)).thenReturn(Optional.of(indeks));
        StudentStatusChangeRequest request = change("MIROVANJE");
        request.setValidFrom(LocalDate.now());
        request.setValidTo(LocalDate.now().plusMonths(2));

        service.changeStatus(1L, request);

        assertThat(indeks.getStatus()).isEqualTo(StudentStatus.MIROVANJE);
        assertThat(indeks.isAktivan()).isFalse();
        verify(historyRepo).save(any(StudentStatusHistory.class));
        verify(auditRepo).save(any());
    }

    @Test
    void graduatedToActiveIsRejected() {
        StudentIndeks indeks = indeks(StudentStatus.DIPLOMIRAO, false);
        when(indeksRepo.findById(1L)).thenReturn(Optional.of(indeks));

        assertThatThrownBy(() -> service.changeStatus(1L, change("AKTIVAN")))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Nedozvoljena");
        verify(historyRepo, never()).save(any());
    }

    @Test
    void dormantStudentCannotPerformAcademicActions() {
        StudentIndeks indeks = indeks(StudentStatus.MIROVANJE, false);
        when(indeksRepo.findById(1L)).thenReturn(Optional.of(indeks));

        assertThatThrownBy(() -> service.assertAcademicallyActive(1L))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("AKTIVAN");
    }

    private StudentStatusChangeRequest change(String status) {
        StudentStatusChangeRequest request = new StudentStatusChangeRequest();
        request.setNewStatus(status);
        request.setReason("Test reason");
        return request;
    }

    private StudentIndeks indeks(StudentStatus status, boolean active) {
        StudentIndeks indeks = new StudentIndeks();
        indeks.setId(1L);
        indeks.setStatus(status);
        indeks.setAktivan(active);
        return indeks;
    }
}
