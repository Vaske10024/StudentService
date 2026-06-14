package org.raflab.studsluzba.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.UpisGodine;
import org.raflab.studsluzba.model.dtos.StudyYearEnrollmentEligibilityDTO;
import org.raflab.studsluzba.model.dtos.StudyYearEnrollmentRequestCreateDTO;
import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequest;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.enrollment.StudyYearEnrollmentRequestHistoryRepository;
import org.raflab.studsluzba.repositories.enrollment.StudyYearEnrollmentRequestRepository;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.utils.EntityMappers;
import org.springframework.security.access.AccessDeniedException;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StudyYearEnrollmentServiceTest {

    @Mock StudyYearEnrollmentRequestRepository requestRepo;
    @Mock StudyYearEnrollmentRequestHistoryRepository historyRepo;
    @Mock StudentIndeksRepository indeksRepo;
    @Mock UpisGodineRepository upisRepo;
    @Mock ObnovaGodineRepository obnovaRepo;
    @Mock SkolskaGodinaRepository schoolYearRepo;
    @Mock SlusaPredmetRepository slusaRepo;
    @Mock ProgramPredmetRepository programSubjectRepo;
    @Mock IspitQueryRepository examRepo;
    @Mock AuditLogRepository auditRepo;
    @Mock AcademicProgressService academicProgressService;
    @Mock StudentIspitiViewService studentIspitiViewService;
    @Mock ECTSRuleService ectsRuleService;
    @Mock StudyYearEnrollmentPolicy policy;
    @Mock RealizacijaPredmetaService realizacijaService;
    @Mock SlusaPredmetService slusaPredmetService;
    @Mock NotificationService notificationService;
    @Mock PermissionService permissions;
    @Mock CurrentUser currentUser;
    @Mock EntityMappers mappers;

    @Spy
    @InjectMocks
    StudyYearEnrollmentService service;

    private StudentIndeks indeks;
    private SkolskaGodina currentYear;
    private SkolskaGodina targetYear;

    @BeforeEach
    void setUp() {
        StudijskiProgram program = new StudijskiProgram();
        program.setId(10L);
        program.setTrajanjeGodina(4);
        StudentPodaci student = new StudentPodaci();
        student.setIme("Ana");
        student.setPrezime("Anic");
        indeks = new StudentIndeks();
        indeks.setId(1L);
        indeks.setBroj(12);
        indeks.setGodina(2025);
        indeks.setStudProgramOznaka("RN");
        indeks.setStudijskiProgram(program);
        indeks.setStudent(student);

        currentYear = schoolYear(20L, "2025/2026");
        targetYear = schoolYear(21L, "2026/2027");

        when(currentUser.userId()).thenReturn(7L);
        when(historyRepo.findByRequestIdOrderByCreatedAtAsc(anyLong())).thenReturn(Collections.emptyList());
        when(requestRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(upisRepo.save(any())).thenAnswer(invocation -> {
            UpisGodine saved = invocation.getArgument(0);
            saved.setId(99L);
            return saved;
        });
    }

    @Test
    void rejectsDuplicateActiveRequestForTargetSchoolYear() {
        stubStudentIdentity();
        doReturn(eligibility()).when(service).eligibility(indeks);
        when(schoolYearRepo.findById(currentYear.getId())).thenReturn(Optional.of(currentYear));
        when(schoolYearRepo.findById(targetYear.getId())).thenReturn(Optional.of(targetYear));
        when(requestRepo.existsByStudentIndeksIdAndTargetSchoolYearIdAndStatusIn(eq(1L), eq(21L), anyCollection()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.submit(createRequest(Collections.emptySet())))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Vec postoji aktivan zahtev");
    }

    @Test
    void passedSubjectCannotBeTransferred() {
        stubStudentIdentity();
        doReturn(eligibility()).when(service).eligibility(indeks);
        when(schoolYearRepo.findById(currentYear.getId())).thenReturn(Optional.of(currentYear));
        when(schoolYearRepo.findById(targetYear.getId())).thenReturn(Optional.of(targetYear));
        when(examRepo.existsPassedSubject(1L, 55L)).thenReturn(true);

        assertThatThrownBy(() -> service.submit(createRequest(Collections.singleton(55L))))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Polozeni predmet");
    }

    @Test
    void studentCannotApproveOwnRequest() {
        doThrow(new AccessDeniedException("Potrebna je ADMIN uloga.")).when(currentUser).requireAdmin();

        assertThatThrownBy(() -> service.approve(1L))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("ADMIN");
        verify(upisRepo, never()).save(any());
    }

    @Test
    void adminApprovalCreatesRealEnrollmentAndApprovesRequest() {
        StudyYearEnrollmentRequest request = requestReadyForApproval();
        when(requestRepo.findById(1L)).thenReturn(Optional.of(request));
        when(indeksRepo.findByIdForUpdate(1L)).thenReturn(indeks);
        when(slusaRepo.findAllForStudent(1L)).thenReturn(Collections.emptyList());
        when(academicProgressService.recalculateEarnedEcts(1L)).thenReturn(55);
        when(ectsRuleService.minimumEctsFor(indeks, 2, 48)).thenReturn(48);
        when(policy.regularThresholdForCurrentYear(1)).thenReturn(48);
        when(policy.conditionalThresholdForCurrentYear(1)).thenReturn(37);
        when(realizacijaService.ensureForEnrollment(10L, 2, 21L)).thenReturn(Collections.emptyList());

        var result = service.approve(1L);

        assertThat(result.getStatus()).isEqualTo("APPROVED");
        assertThat(request.getApprovedEnrollment()).isNotNull();
        assertThat(request.getApprovedEnrollment().getUpisujeGodinu()).isEqualTo(2);
        assertThat(request.getApprovedEnrollment().getSkolskaGodina()).isEqualTo(targetYear);
        verify(upisRepo).save(any(UpisGodine.class));
        verify(slusaPredmetService).enrollRealizations(eq(indeks), any(UpisGodine.class), anyList());
    }

    @Test
    void adminCanReturnRequestForChangesWithReason() {
        StudyYearEnrollmentRequest request = requestReadyForApproval();
        when(requestRepo.findById(1L)).thenReturn(Optional.of(request));

        var result = service.needsChanges(1L, "Nedostaje potvrda.");

        assertThat(result.getStatus()).isEqualTo("NEEDS_CHANGES");
        assertThat(request.getAdminNote()).isEqualTo("Nedostaje potvrda.");
        verify(historyRepo).save(any());
    }

    private void stubStudentIdentity() {
        when(currentUser.role()).thenReturn(Role.STUDENT);
        when(currentUser.linkedStudentIndeksId()).thenReturn(1L);
        when(indeksRepo.findById(1L)).thenReturn(Optional.of(indeks));
        when(indeksRepo.findByIdForUpdate(1L)).thenReturn(indeks);
    }

    private StudyYearEnrollmentEligibilityDTO eligibility() {
        StudyYearEnrollmentEligibilityDTO dto = new StudyYearEnrollmentEligibilityDTO();
        dto.setCanSubmit(true);
        dto.setCurrentStudyYear(1);
        dto.setRequestedStudyYear(2);
        dto.setEarnedEcts(55);
        dto.setSuggestedType("ENROLL_NEXT_YEAR");
        var currentDto = new org.raflab.studsluzba.model.dtos.SkolskaGodinaDTO();
        currentDto.setId(currentYear.getId());
        currentDto.setGodina(currentYear.getGodina());
        var targetDto = new org.raflab.studsluzba.model.dtos.SkolskaGodinaDTO();
        targetDto.setId(targetYear.getId());
        targetDto.setGodina(targetYear.getGodina());
        dto.setCurrentSchoolYear(currentDto);
        dto.setTargetSchoolYear(targetDto);
        return dto;
    }

    private StudyYearEnrollmentRequestCreateDTO createRequest(java.util.Set<Long> subjects) {
        StudyYearEnrollmentRequestCreateDTO dto = new StudyYearEnrollmentRequestCreateDTO();
        dto.setType("ENROLL_NEXT_YEAR");
        dto.setTargetSchoolYearId(targetYear.getId());
        dto.setTransferredSubjectIds(subjects);
        return dto;
    }

    private StudyYearEnrollmentRequest requestReadyForApproval() {
        StudyYearEnrollmentRequest request = new StudyYearEnrollmentRequest();
        request.setId(1L);
        request.setStudentIndeks(indeks);
        request.setCurrentSchoolYear(currentYear);
        request.setTargetSchoolYear(targetYear);
        request.setType(StudyYearEnrollmentRequest.Type.ENROLL_NEXT_YEAR);
        request.setStatus(StudyYearEnrollmentRequest.Status.PENDING_ADMIN_APPROVAL);
        request.setCurrentStudyYear(1);
        request.setRequestedStudyYear(2);
        request.setEarnedEctsSnapshot(50);
        request.setContractReceived(true);
        request.setPaymentConfirmed(true);
        request.setDocumentationComplete(true);
        return request;
    }

    private SkolskaGodina schoolYear(Long id, String value) {
        SkolskaGodina result = new SkolskaGodina();
        result.setId(id);
        result.setGodina(value);
        return result;
    }
}
