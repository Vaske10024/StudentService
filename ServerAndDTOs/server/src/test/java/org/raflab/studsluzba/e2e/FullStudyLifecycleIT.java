package org.raflab.studsluzba.e2e;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.StudentStatus;
import org.raflab.studsluzba.model.dtos.IspitIzlazakRequest;
import org.raflab.studsluzba.model.dtos.PrijavaCreateRequest;
import org.raflab.studsluzba.model.dtos.StudyYearEnrollmentChecklistDTO;
import org.raflab.studsluzba.model.dtos.StudyYearEnrollmentRequestCreateDTO;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.ispiti.PrijavaStatus;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.IspitniRokRepository;
import org.raflab.studsluzba.repositories.ObnovaGodineRepository;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.raflab.studsluzba.repositories.RealizacijaPredmetaRepository;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;
import org.raflab.studsluzba.repositories.SlusaPredmetRepository;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.raflab.studsluzba.repositories.UpisGodineRepository;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.AcademicProgressService;
import org.raflab.studsluzba.services.DrziPredmetAdminService;
import org.raflab.studsluzba.services.IspitAdminService;
import org.raflab.studsluzba.services.IspitCommandService;
import org.raflab.studsluzba.services.OstvarenaPredObavService;
import org.raflab.studsluzba.services.PredispitnaObavezaService;
import org.raflab.studsluzba.services.PredmetAdminService;
import org.raflab.studsluzba.services.RealizacijaPredmetaService;
import org.raflab.studsluzba.services.SkolskaGodinaService;
import org.raflab.studsluzba.services.StudentProfileService;
import org.raflab.studsluzba.services.StudijskiProgramAdminService;
import org.raflab.studsluzba.services.StudyYearEnrollmentService;
import org.raflab.studsluzba.services.UpisObnovaService;
import org.raflab.studsluzba.services.UserAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class FullStudyLifecycleIT {
    @Autowired EntityManager em;
    @Autowired StudijskiProgramAdminService programAdminService;
    @Autowired PredmetAdminService predmetAdminService;
    @Autowired SkolskaGodinaService schoolYearService;
    @Autowired RealizacijaPredmetaService realizationService;
    @Autowired DrziPredmetAdminService assignmentService;
    @Autowired IspitAdminService examAdminService;
    @Autowired UserAccountService accountService;
    @Autowired UpisObnovaService upisObnovaService;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired StudijskiProgramRepository programRepo;
    @Autowired PredmetRepository predmetRepo;
    @Autowired SkolskaGodinaRepository schoolYearRepo;
    @Autowired RealizacijaPredmetaRepository realizationRepo;
    @Autowired DrziPredmetRepository assignmentRepo;
    @Autowired IspitRepository examRepo;
    @Autowired IspitniRokRepository periodRepo;
    @Autowired UserAccountRepository accountRepo;

    @Autowired IspitCommandService examCommandService;
    @Autowired PredispitnaObavezaService preExamDefinitionService;
    @Autowired OstvarenaPredObavService preExamScoreService;
    @Autowired StudyYearEnrollmentService yearEnrollmentService;
    @Autowired AcademicProgressService progressService;
    @Autowired StudentProfileService profileService;
    @Autowired SlusaPredmetRepository listeningRepo;
    @Autowired IspitQueryRepository attemptRepo;
    @Autowired UpisGodineRepository enrollmentRepo;
    @Autowired ObnovaGodineRepository renewalRepo;
    @Autowired CurrentUser currentUser;

    private E2ETestDataFactory factory;

    @BeforeEach
    void setUp() {
        factory = new E2ETestDataFactory(em, programAdminService, predmetAdminService, schoolYearService,
                realizationService, assignmentService, examAdminService, accountService, upisObnovaService,
                passwordEncoder, programRepo, predmetRepo, schoolYearRepo, realizationRepo, assignmentRepo,
                examRepo, periodRepo, accountRepo);
    }

    @Test
    void fullStudentLifecycleCoversSetupStudyExamsHistoryAndNextYearEnrollment() {
        E2ETestDataFactory.Dataset data = factory.createAcademicDataset();
        E2ETestDataFactory.StudentActor student = factory.createStudent(data, "Glavni", 1);
        authenticate(student.provision.getAccount());

        assertThat(student.provision.isCreated()).isTrue();
        assertThat(student.provision.getAccount().isMustChangePassword()).isTrue();
        accountService.changeCurrentPassword(student.provision.getTemporaryPassword(), "Changed-E2E-Password-123!");
        assertThat(accountRepo.findById(student.provision.getAccount().getId()).orElseThrow(AssertionError::new)
                .isMustChangePassword()).isFalse();

        assertThat(profileService.getStudentDashboard(student.index.getId()).getCurrentSubjects()).hasSize(4);
        StudyPipelineScenarioAssertions.assertUniqueListeningRecords(listeningRepo.findAllForStudent(student.index.getId()));

        Predmet foreignSubject = data.subjectsByYear.get(2).get(0);
        IspitniRok foreignPeriod = factory.createPeriod(data.schoolYears.get(1), 90);
        Ispit foreignExam = factory.createExam(data.offering(foreignSubject), foreignPeriod, 0);
        assertThat(examCommandService.eligibility(foreignExam.getId(), student.index.getId()).getCode())
                .isEqualTo("SUBJECT_NOT_ENROLLED");

        Predmet cancelledSubject = data.subjectsByYear.get(1).get(0);
        IspitniRok cancellationPeriod = factory.createPeriod(data.schoolYears.get(0), 70);
        Ispit cancellationExam = factory.createExam(data.offering(cancelledSubject), cancellationPeriod, 0);
        Long cancelledId = register(cancellationExam, student.index);
        examCommandService.odjavi(cancelledId, "Student bira drugi termin");
        PrijavaIspita cancelled = attemptRepo.findById(cancelledId).orElseThrow(AssertionError::new);
        assertThat(cancelled.getStatus()).isEqualTo(PrijavaStatus.ODJAVLJEN);
        assertThat(cancelled.getCancellationReason()).isEqualTo("Student bira drugi termin");
        assertThat(attemptRepo.previousAttemptsForStudent(student.index.getId())).extracting(PrijavaIspita::getId)
                .contains(cancelledId);
        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isZero();

        IspitniRok firstYearPeriod = factory.createPeriod(data.schoolYears.get(0), 0);
        for (Predmet subject : data.subjectsByYear.get(1)) {
            Ispit exam = factory.createExam(data.offering(subject), firstYearPeriod, data.subjectsByYear.get(1).indexOf(subject));
            passExam(student.index, subject, data.schoolYears.get(0).getId(), exam, 30, 40);
        }

        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isEqualTo(60);
        assertThat(progressService.averageGrade(student.index.getId())).isEqualTo(7.0);
        assertThat(profileService.getStudentDashboard(student.index.getId()).getPassedSubjects()).hasSize(4);

        authenticate(student.provision.getAccount());
        var eligibility = yearEnrollmentService.myEligibility();
        assertThat(eligibility.getSuggestedType()).isEqualTo("ENROLL_NEXT_YEAR");
        var submitted = yearEnrollmentService.submit(yearRequest(eligibility.getSuggestedType(),
                eligibility.getTargetSchoolYear().getId(), Collections.emptySet()));
        assertThatThrownBy(() -> yearEnrollmentService.submit(yearRequest(eligibility.getSuggestedType(),
                eligibility.getTargetSchoolYear().getId(), Collections.emptySet())))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("DUPLICATE_ACTIVE_YEAR_REQUEST");

        authenticate(data.admin);
        assertThat(yearEnrollmentService.adminList(null, null, null, student.index.getId())).hasSize(1);
        yearEnrollmentService.updateChecklist(submitted.getId(), checklist(true, false, true));
        assertThatThrownBy(() -> yearEnrollmentService.approve(submitted.getId()))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("YEAR_REQUEST_NOT_READY");
        yearEnrollmentService.updateChecklist(submitted.getId(), checklist(true, true, true));
        var approved = yearEnrollmentService.approve(submitted.getId());
        assertThat(approved.getStatus()).isEqualTo("APPROVED");
        assertThat(enrollmentRepo.findUpisi(student.index.getId())).extracting(item -> item.getUpisujeGodinu())
                .containsExactly(2, 1);

        factory.activate(data.schoolYears.get(1));
        assertThat(profileService.currentSubjects(student.index.getId())).hasSize(4);

        Predmet retakeSubject = data.subjectsByYear.get(2).get(0);
        Long definitionId = preExamDefinitionService.create(retakeSubject.getId(), data.schoolYears.get(1).getId(),
                "E2E kolokvijum", 10);
        preExamScoreService.upsert(student.index.getId(), definitionId, 10);
        IspitniRok secondYearFirstPeriod = factory.createPeriod(data.schoolYears.get(1), 10);
        Ispit failedExam = factory.createExam(data.offering(retakeSubject), secondYearFirstPeriod, 0);
        Long failedAttemptId = register(failedExam, student.index);
        recordExit(failedAttemptId, 20);
        authenticate(data.admin);
        examAdminService.lock(failedExam.getId());

        IspitniRok secondYearRetakePeriod = factory.createPeriod(data.schoolYears.get(1), 30);
        Ispit passedRetake = factory.createExam(data.offering(retakeSubject), secondYearRetakePeriod, 0);
        authenticate(student.provision.getAccount());
        Long passedAttemptId = register(passedRetake, student.index);
        recordExit(passedAttemptId, 50);
        authenticate(data.admin);
        examAdminService.lock(passedRetake.getId());

        List<PrijavaIspita> attempts = attemptRepo.previousAttemptsForStudent(student.index.getId());
        StudyPipelineScenarioAssertions.assertAttemptHistory(attempts, PrijavaStatus.PAO, PrijavaStatus.POLOZIO,
                PrijavaStatus.ODJAVLJEN);
        assertThat(attemptRepo.brojPolaganja(student.index.getId(), retakeSubject.getId())).isEqualTo(2);
        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isEqualTo(75);

        IspitniRok alreadyPassedPeriod = factory.createPeriod(data.schoolYears.get(1), 50);
        Ispit alreadyPassedExam = factory.createExam(data.offering(retakeSubject), alreadyPassedPeriod, 0);
        assertThat(examCommandService.eligibility(alreadyPassedExam.getId(), student.index.getId()).getCode())
                .isEqualTo("SUBJECT_ALREADY_PASSED");

        Predmet voidedSubject = data.subjectsByYear.get(2).get(1);
        Ispit voidedExam = factory.createExam(data.offering(voidedSubject), secondYearRetakePeriod, 1);
        authenticate(student.provision.getAccount());
        Long voidedId = register(voidedExam, student.index);
        authenticate(data.admin);
        examCommandService.ponisti(voidedId, "Administrativno ponistavanje");
        PrijavaIspita voided = attemptRepo.findById(voidedId).orElseThrow(AssertionError::new);
        assertThat(voided.getStatus()).isEqualTo(PrijavaStatus.PONISTEN);
        assertThat(voided.getCancellationReason()).isEqualTo("Administrativno ponistavanje");
        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isEqualTo(75);

        assertThat(upisObnovaService.syncCurrentSubjects(student.index.getId())).isEqualTo(4);
        assertThat(upisObnovaService.syncCurrentSubjects(student.index.getId())).isEqualTo(4);
        StudyPipelineScenarioAssertions.assertUniqueListeningRecords(listeningRepo.findAllForStudent(student.index.getId()));

        Nastavnik otherProfessor = new Nastavnik();
        otherProfessor.setIme("Drugi");
        otherProfessor.setPrezime("Profesor");
        otherProfessor.setEmail(data.prefix.toLowerCase() + ".other.professor@example.test");
        otherProfessor.setJmbg(data.prefix + "_OTHER_PROF");
        em.persist(otherProfessor);
        em.flush();
        UserAccount otherProfessorAccount = accountService.provisionProfessorAccountWithCredential(otherProfessor).getAccount();
        authenticate(otherProfessorAccount);
        assertThatThrownBy(() -> currentUser.requireProfessorOwnsIspit(alreadyPassedExam.getId()))
                .isInstanceOf(AccessDeniedException.class);

        completeYear(data, student, 2, 70);
        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isEqualTo(120);
        enrollNextYear(data, student, 2);

        completeYear(data, student, 3, 0);
        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isEqualTo(180);
        enrollNextYear(data, student, 3);

        completeYear(data, student, 4, 0);
        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isEqualTo(240);
        assertThat(progressService.averageGrade(student.index.getId())).isEqualTo(6.9375);
        assertThat(profileService.getStudentDashboard(student.index.getId()).getPassedSubjects()).hasSize(16);
        assertThat(enrollmentRepo.findUpisi(student.index.getId())).extracting(item -> item.getUpisujeGodinu())
                .containsExactly(4, 3, 2, 1);
        StudyPipelineScenarioAssertions.assertUniqueListeningRecords(listeningRepo.findAllForStudent(student.index.getId()));
    }

    @Test
    void conditionalEnrollmentAndRenewalUseTransferredSubjectsWithoutDuplicates() {
        E2ETestDataFactory.Dataset data = factory.createAcademicDataset();
        E2ETestDataFactory.StudentActor conditional = factory.createStudent(data, "Uslovni", 2);
        E2ETestDataFactory.StudentActor renewal = factory.createStudent(data, "Obnova", 3);

        authenticate(data.admin);
        for (int i = 0; i < 3; i++) {
            examCommandService.priznajPredmet(conditional.index.getId(), data.subjectsByYear.get(1).get(i).getId(), 8,
                    "E2E priznati predmet");
        }

        authenticate(conditional.provision.getAccount());
        var conditionalEligibility = yearEnrollmentService.myEligibility();
        assertThat(conditionalEligibility.getEarnedEcts()).isEqualTo(45);
        assertThat(conditionalEligibility.getSuggestedType()).isEqualTo("CONDITIONAL_ENROLLMENT");
        Predmet conditionalTransfer = data.subjectsByYear.get(1).get(3);
        var conditionalRequest = yearEnrollmentService.submit(yearRequest("CONDITIONAL_ENROLLMENT",
                conditionalEligibility.getTargetSchoolYear().getId(), Set.of(conditionalTransfer.getId())));

        authenticate(data.admin);
        yearEnrollmentService.updateChecklist(conditionalRequest.getId(), checklist(true, true, true));
        yearEnrollmentService.approve(conditionalRequest.getId());

        authenticate(renewal.provision.getAccount());
        var renewalEligibility = yearEnrollmentService.myEligibility();
        assertThat(renewalEligibility.getEarnedEcts()).isZero();
        assertThat(renewalEligibility.getSuggestedType()).isEqualTo("RENEW_YEAR");
        Set<Long> renewalSubjects = Set.of(data.subjectsByYear.get(1).get(0).getId(),
                data.subjectsByYear.get(1).get(1).getId());
        var renewalRequest = yearEnrollmentService.submit(yearRequest("RENEW_YEAR",
                renewalEligibility.getTargetSchoolYear().getId(), renewalSubjects));

        authenticate(data.admin);
        yearEnrollmentService.updateChecklist(renewalRequest.getId(), checklist(true, true, true));
        yearEnrollmentService.approve(renewalRequest.getId());
        assertThat(renewalRepo.findObnove(renewal.index.getId())).hasSize(1);
        assertThat(enrollmentRepo.findUpisi(renewal.index.getId())).extracting(item -> item.getUpisujeGodinu())
                .containsExactly(1, 1);

        factory.activate(data.schoolYears.get(1));
        assertThat(profileService.currentSubjects(conditional.index.getId())).hasSize(5);
        assertThat(profileService.currentSubjects(renewal.index.getId())).hasSize(2);
        StudyPipelineScenarioAssertions.assertUniqueListeningRecords(listeningRepo.findAllForStudent(conditional.index.getId()));
        StudyPipelineScenarioAssertions.assertUniqueListeningRecords(listeningRepo.findAllForStudent(renewal.index.getId()));
    }

    @Test
    void absentStudentWithHighManualGradeDoesNotEarnEctsOrAppearPassed() {
        E2ETestDataFactory.Dataset data = factory.createAcademicDataset();
        E2ETestDataFactory.StudentActor student = factory.createStudent(data, "Odsutan", 4);
        Predmet subject = data.subjectsByYear.get(1).get(0);
        Ispit exam = factory.createExam(data.offering(subject), factory.createPeriod(data.schoolYears.get(0), 0), 0);
        authenticate(student.provision.getAccount());
        Long attemptId = register(exam, student.index);
        PrijavaIspita attempt = attemptRepo.findById(attemptId).orElseThrow(AssertionError::new);
        attempt.setOcena(10);
        attempt.setDaLiJeIzasao(false);
        attemptRepo.save(attempt);
        em.flush();

        assertThat(attemptRepo.existsPassedSubject(student.index.getId(), subject.getId())).isFalse();
        assertThat(progressService.calculateEarnedEcts(student.index.getId())).isZero();
        assertThat(progressService.averageGrade(student.index.getId())).isZero();
    }

    @Test
    void indexWithoutEnrollmentHasNoCurrentSubjects() {
        E2ETestDataFactory.Dataset data = factory.createAcademicDataset();
        StudentPodaci personal = new StudentPodaci();
        personal.setIme("Bez");
        personal.setPrezime("Upisa");
        personal.setJmbg(data.prefix + "_NO_ENROLLMENT");
        em.persist(personal);
        StudentIndeks index = new StudentIndeks();
        index.setStudent(personal);
        index.setStudijskiProgram(data.program);
        index.setStudProgramOznaka(data.program.getOznaka());
        index.setGodina(2100);
        index.setBroj(99);
        index.setAktivan(false);
        index.setStatus(StudentStatus.NEAKTIVAN);
        em.persist(index);
        em.flush();

        assertThat(profileService.currentSubjects(index.getId())).isEmpty();
    }

    private void passExam(StudentIndeks student, Predmet subject, Long schoolYearId, Ispit exam,
                          int preExamPoints, int examPoints) {
        Long definitionId = preExamDefinitionService.create(subject.getId(), schoolYearId,
                "E2E predispit " + subject.getSifra(), preExamPoints);
        preExamScoreService.upsert(student.getId(), definitionId, preExamPoints);
        Long attemptId = register(exam, student);
        recordExit(attemptId, examPoints);
        examAdminService.lock(exam.getId());
    }

    private void completeYear(E2ETestDataFactory.Dataset data, E2ETestDataFactory.StudentActor student,
                              int studyYear, int periodOffset) {
        authenticate(student.provision.getAccount());
        IspitniRok period = factory.createPeriod(data.schoolYears.get(studyYear - 1), periodOffset);
        int dayOffset = 0;
        for (Predmet subject : data.subjectsByYear.get(studyYear)) {
            if (!attemptRepo.existsPassedSubject(student.index.getId(), subject.getId())) {
                Ispit exam = factory.createExam(data.offering(subject), period, dayOffset++);
                passExam(student.index, subject, data.schoolYears.get(studyYear - 1).getId(), exam, 30, 40);
            }
        }
    }

    private void enrollNextYear(E2ETestDataFactory.Dataset data, E2ETestDataFactory.StudentActor student,
                                int currentStudyYear) {
        authenticate(student.provision.getAccount());
        var eligibility = yearEnrollmentService.myEligibility();
        assertThat(eligibility.getCurrentStudyYear()).isEqualTo(currentStudyYear);
        assertThat(eligibility.getSuggestedType()).isEqualTo("ENROLL_NEXT_YEAR");
        var request = yearEnrollmentService.submit(yearRequest("ENROLL_NEXT_YEAR",
                eligibility.getTargetSchoolYear().getId(), Collections.emptySet()));

        authenticate(data.admin);
        yearEnrollmentService.updateChecklist(request.getId(), checklist(true, true, true));
        yearEnrollmentService.approve(request.getId());
        factory.activate(data.schoolYears.get(currentStudyYear));

        authenticate(student.provision.getAccount());
        assertThat(profileService.currentSubjects(student.index.getId())).hasSize(4);
    }

    private Long register(Ispit exam, StudentIndeks student) {
        PrijavaCreateRequest request = new PrijavaCreateRequest();
        request.setIspitId(exam.getId());
        request.setStudentIndeksId(student.getId());
        return examCommandService.prijaviStudenta(request);
    }

    private void recordExit(Long attemptId, int points) {
        IspitIzlazakRequest request = new IspitIzlazakRequest();
        request.setPrijavaId(attemptId);
        request.setBrojOsvojenihPoena(points);
        examCommandService.evidentirajIzlazak(request);
    }

    private StudyYearEnrollmentRequestCreateDTO yearRequest(String type, Long targetSchoolYearId, Set<Long> subjects) {
        StudyYearEnrollmentRequestCreateDTO request = new StudyYearEnrollmentRequestCreateDTO();
        request.setType(type);
        request.setTargetSchoolYearId(targetSchoolYearId);
        request.setTransferredSubjectIds(subjects);
        request.setStudentNote("E2E lifecycle request");
        return request;
    }

    private StudyYearEnrollmentChecklistDTO checklist(boolean contract, boolean payment, boolean documents) {
        StudyYearEnrollmentChecklistDTO checklist = new StudyYearEnrollmentChecklistDTO();
        checklist.setContractReceived(contract);
        checklist.setPaymentConfirmed(payment);
        checklist.setDocumentationComplete(documents);
        checklist.setNote("E2E checklist");
        return checklist;
    }

    private void authenticate(UserAccount account) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(account.getUsername(), "n/a", Collections.emptyList()));
    }
}
