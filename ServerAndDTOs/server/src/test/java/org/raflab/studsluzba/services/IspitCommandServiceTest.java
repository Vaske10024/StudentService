package org.raflab.studsluzba.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.*;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.model.dtos.PrijavaCreateRequest;
import org.raflab.studsluzba.model.dtos.PrijavaResultUpdateRequest;
import org.raflab.studsluzba.model.dtos.IspitIzlazakRequest;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class IspitCommandServiceTest {
    private IspitRepository ispitRepo;
    private IspitQueryRepository prijavaRepo;
    private StudentIndeksRepository indeksRepo;
    private IspitQueryService queryService;
    private SlusaPredmetRepository slusaRepo;
    private PredmetRepository predmetRepo;
    private UserAccountRepository userRepo;
    private GradingService gradingService;
    private CurrentUser currentUser;
    private StudentLifecycleService studentLifecycleService;
    private AuditLogRepository auditLogRepo;
    private DebtPolicyService debtPolicyService;
    private PrerequisiteService prerequisiteService;
    private NotificationService notificationService;
    private IspitCommandService service;

    @BeforeEach
    void setUp() {
        ispitRepo = mock(IspitRepository.class);
        prijavaRepo = mock(IspitQueryRepository.class);
        indeksRepo = mock(StudentIndeksRepository.class);
        queryService = mock(IspitQueryService.class);
        slusaRepo = mock(SlusaPredmetRepository.class);
        predmetRepo = mock(PredmetRepository.class);
        userRepo = mock(UserAccountRepository.class);
        gradingService = mock(GradingService.class);
        currentUser = mock(CurrentUser.class);
        studentLifecycleService = mock(StudentLifecycleService.class);
        auditLogRepo = mock(AuditLogRepository.class);
        debtPolicyService = mock(DebtPolicyService.class);
        prerequisiteService = mock(PrerequisiteService.class);
        notificationService = mock(NotificationService.class);
        service = new IspitCommandService(ispitRepo, prijavaRepo, indeksRepo, queryService, slusaRepo, predmetRepo, userRepo, gradingService, currentUser, studentLifecycleService, auditLogRepo, debtPolicyService, prerequisiteService, notificationService, mock(AcademicProgressService.class));
    }

    @Test
    void registrationRequiresStudentToListenSubjectInActiveSchoolYear() {
        StudentIndeks indeks = indeks(10L, true);
        Ispit ispit = ispit(20L, 30L, false);
        when(ispitRepo.findById(20L)).thenReturn(Optional.of(ispit));
        when(indeksRepo.findById(10L)).thenReturn(Optional.of(indeks));
        when(slusaRepo.existsStudentSlusaPredmetAktivna(10L, 30L)).thenReturn(false);

        PrijavaCreateRequest req = new PrijavaCreateRequest();
        req.setIspitId(20L);
        req.setStudentIndeksId(10L);

        assertThatThrownBy(() -> service.prijaviStudenta(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("slušanje");
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void registrationRejectsLockedExam() {
        StudentIndeks indeks = indeks(10L, true);
        Ispit ispit = ispit(20L, 30L, true);
        when(ispitRepo.findById(20L)).thenReturn(Optional.of(ispit));
        when(indeksRepo.findById(10L)).thenReturn(Optional.of(indeks));

        PrijavaCreateRequest req = new PrijavaCreateRequest();
        req.setIspitId(20L);
        req.setStudentIndeksId(10L);

        assertThatThrownBy(() -> service.prijaviStudenta(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("zaključan");
    }

    @Test
    void registrationRejectsDisabledStudentAccount() {
        StudentIndeks indeks = indeks(10L, true);
        Ispit ispit = ispit(20L, 30L, false);
        UserAccount disabled = new UserAccount();
        disabled.setRole(Role.STUDENT);
        disabled.setEnabled(false);
        when(ispitRepo.findById(20L)).thenReturn(Optional.of(ispit));
        when(indeksRepo.findById(10L)).thenReturn(Optional.of(indeks));
        when(userRepo.findByLinkedStudentIndeksId(10L)).thenReturn(Optional.of(disabled));

        PrijavaCreateRequest req = new PrijavaCreateRequest();
        req.setIspitId(20L);
        req.setStudentIndeksId(10L);

        assertThatThrownBy(() -> service.prijaviStudenta(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("nalog nije aktivan");
    }

    @Test
    void registrationRejectsAcademicallyInactiveStudent() {
        StudentIndeks indeks = indeks(10L, false);
        Ispit ispit = ispit(20L, 30L, false);
        when(ispitRepo.findById(20L)).thenReturn(Optional.of(ispit));
        when(indeksRepo.findById(10L)).thenReturn(Optional.of(indeks));
        doThrow(ApiException.conflict("STUDENT_NOT_ACADEMICALLY_ACTIVE", "Student nije akademski aktivan."))
                .when(studentLifecycleService).assertAcademicallyActive(10L);

        PrijavaCreateRequest req = new PrijavaCreateRequest();
        req.setIspitId(20L);
        req.setStudentIndeksId(10L);

        assertThatThrownBy(() -> service.prijaviStudenta(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("akademski aktivan");
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void registrationRejectsDebtAboveConfiguredLimit() {
        StudentIndeks indeks = indeks(10L, true);
        Ispit ispit = ispit(20L, 30L, false);
        when(ispitRepo.findById(20L)).thenReturn(Optional.of(ispit));
        when(indeksRepo.findById(10L)).thenReturn(Optional.of(indeks));
        doThrow(ApiException.conflict("DEBT_BLOCKS_EXAM_REGISTRATION", "Dug blokira prijavu."))
                .when(debtPolicyService).assertExamRegistrationAllowed(10L);

        PrijavaCreateRequest req = new PrijavaCreateRequest();
        req.setIspitId(20L);
        req.setStudentIndeksId(10L);

        assertThatThrownBy(() -> service.prijaviStudenta(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("Dug blokira");
        verify(prijavaRepo, never()).save(any());
    }

    @Test
    void registrationBeforeWindowReturnsNotOpen() {
        StudentIndeks indeks = indeks(10L, true);
        Ispit ispit = ispit(20L, 30L, false);
        ispit.getIspitniRok().setRegistrationStart(LocalDateTime.now().plusHours(1));
        ispit.getIspitniRok().setRegistrationEnd(LocalDateTime.now().plusHours(2));
        stubEligibleRegistration(indeks, ispit);
        PrijavaCreateRequest req = request(10L, 20L);
        assertThatThrownBy(() -> service.prijaviStudenta(req))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("REGISTRATION_NOT_OPEN");
    }

    @Test
    void registrationAfterWindowReturnsClosed() {
        StudentIndeks indeks = indeks(10L, true);
        Ispit ispit = ispit(20L, 30L, false);
        ispit.getIspitniRok().setRegistrationStart(LocalDateTime.now().minusHours(2));
        ispit.getIspitniRok().setRegistrationEnd(LocalDateTime.now().minusHours(1));
        stubEligibleRegistration(indeks, ispit);
        assertThatThrownBy(() -> service.prijaviStudenta(request(10L, 20L)))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("REGISTRATION_CLOSED");
    }

    @Test
    void cancellationAfterWindowReturnsClosed() {
        PrijavaIspita prijava = prijava(99L, ispit(20L, 30L, false));
        prijava.setStudent(indeks(10L, true));
        prijava.setStatus(PrijavaStatus.PRIJAVLJEN);
        prijava.getIspit().getIspitniRok().setCancellationEnd(LocalDateTime.now().minusMinutes(1));
        when(prijavaRepo.findById(99L)).thenReturn(Optional.of(prijava));
        assertThatThrownBy(() -> service.odjavi(99L, "promena plana"))
                .isInstanceOf(ApiException.class).extracting("code").isEqualTo("CANCELLATION_CLOSED");
    }


    @Test
    void lockedExamBlocksResultUpdateForNonAdmin() {
        PrijavaIspita prijava = prijava(99L, ispit(20L, 30L, true));
        when(prijavaRepo.findById(99L)).thenReturn(Optional.of(prijava));
        when(currentUser.isAdmin()).thenReturn(false);
        PrijavaResultUpdateRequest req = new PrijavaResultUpdateRequest();
        req.setPrijavaId(99L);
        req.setBrojOsvojenihPoena(20);

        assertThatThrownBy(() -> service.azurirajRezultat(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("zaključan");
    }

    @Test
    void lockedExamBlocksCancellationForNonAdmin() {
        PrijavaIspita prijava = prijava(99L, ispit(20L, 30L, true));
        when(prijavaRepo.findById(99L)).thenReturn(Optional.of(prijava));
        when(currentUser.isAdmin()).thenReturn(false);

        assertThatThrownBy(() -> service.ponisti(99L, "greska"))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("zaključan");
    }

    @Test
    void lockedExamBlocksAttendanceForNonAdmin() {
        PrijavaIspita prijava = prijava(99L, ispit(20L, 30L, true));
        when(prijavaRepo.findById(99L)).thenReturn(Optional.of(prijava));
        when(currentUser.isAdmin()).thenReturn(false);
        IspitIzlazakRequest req = new IspitIzlazakRequest();
        req.setPrijavaId(99L);
        req.setBrojOsvojenihPoena(20);

        assertThatThrownBy(() -> service.evidentirajIzlazak(req))
                .isInstanceOf(ApiException.class)
                .hasMessageContaining("zaključan");
    }

    private StudentIndeks indeks(Long id, boolean active) {
        StudentIndeks si = new StudentIndeks();
        si.setId(id);
        si.setAktivan(active);
        return si;
    }

    private PrijavaCreateRequest request(Long indeksId, Long ispitId) {
        PrijavaCreateRequest req = new PrijavaCreateRequest();
        req.setStudentIndeksId(indeksId);
        req.setIspitId(ispitId);
        return req;
    }

    private void stubEligibleRegistration(StudentIndeks indeks, Ispit ispit) {
        when(ispitRepo.findById(ispit.getId())).thenReturn(Optional.of(ispit));
        when(indeksRepo.findById(indeks.getId())).thenReturn(Optional.of(indeks));
        when(slusaRepo.existsStudentSlusaPredmetAktivna(indeks.getId(), 30L)).thenReturn(true);
        when(prijavaRepo.findAktivnaPrijava(ispit.getId(), indeks.getId())).thenReturn(Optional.empty());
    }

    private PrijavaIspita prijava(Long id, Ispit ispit) {
        PrijavaIspita pi = new PrijavaIspita();
        pi.setId(id);
        pi.setIspit(ispit);
        pi.setPonisteno(false);
        pi.setDaLiJeIzasao(false);
        pi.setBrojOsvojenihPoena(0);
        pi.setOcena(5);
        return pi;
    }

    private Ispit ispit(Long id, Long predmetId, boolean locked) {
        Predmet p = new Predmet();
        p.setId(predmetId);
        DrziPredmet dp = new DrziPredmet();
        dp.setPredmet(p);
        Ispit i = new Ispit();
        i.setId(id);
        i.setDrziPredmet(dp);
        i.setZakljucen(locked);
        i.setDatumOdrzavanja(LocalDate.now().plusDays(3));
        IspitniRok rok = new IspitniRok();
        rok.setActive(true);
        rok.setRegistrationStart(LocalDateTime.now().minusDays(1));
        rok.setRegistrationEnd(LocalDateTime.now().plusDays(1));
        rok.setCancellationEnd(LocalDateTime.now().plusDays(2));
        i.setIspitniRok(rok);
        return i;
    }
}
