package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.*;
import org.raflab.studsluzba.model.dtos.EnrollmentApplicationCreateDTO;
import org.raflab.studsluzba.model.enrollment.EnrollmentApplication;
import org.raflab.studsluzba.model.enrollment.EnrollmentDecision;
import org.raflab.studsluzba.model.finance.FinancingType;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.enrollment.*;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentWorkflowService {
    private final EnrollmentApplicationRepository applicationRepo;
    private final EnrollmentDecisionRepository decisionRepo;
    private final StudentPodaciRepository studentRepo;
    private final StudentIndeksRepository indeksRepo;
    private final StudijskiProgramRepository programRepo;
    private final UserAccountRepository userRepo;
    private final StudentIndeksService indeksService;
    private final TuitionService tuitionService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;
    private final PermissionService permissions;

    public EnrollmentApplication submit(String idempotencyKey, EnrollmentApplicationCreateDTO dto) {
        if (idempotencyKey == null || idempotencyKey.trim().isEmpty()) throw ApiException.badRequest("Idempotency-Key je obavezan.");
        return applicationRepo.findByIdempotencyKey(idempotencyKey).orElseGet(() -> {
            EnrollmentApplication app = new EnrollmentApplication();
            app.setIdempotencyKey(idempotencyKey);
            app.setStatus(EnrollmentApplication.Status.SUBMITTED);
            app.setIme(dto.getIme()); app.setPrezime(dto.getPrezime()); app.setJmbg(dto.getJmbg());
            app.setEmail(dto.getEmail()); app.setUsername(dto.getUsername());
            app.setStudijskiProgramId(dto.getStudijskiProgramId()); app.setGodina(dto.getGodina());
            app.setTuitionEur(dto.getTuitionEur());
            return applicationRepo.save(app);
        });
    }

    public EnrollmentApplication approve(Long id, String initialPassword) {
        permissions.require(Permission.ENROLLMENT_WRITE);
        EnrollmentApplication app = require(id);
        if (app.getStatus() == EnrollmentApplication.Status.APPROVED) return app;
        if (app.getStatus() != EnrollmentApplication.Status.SUBMITTED) {
            throw ApiException.conflict("ENROLLMENT_NOT_SUBMITTED", "Samo poslata prijava moze biti odobrena.");
        }
        if (studentRepo.existsByJmbg(app.getJmbg()) || studentRepo.existsByEmailFakultetskiIgnoreCase(app.getEmail())
                || userRepo.existsByUsername(app.getUsername())) {
            throw ApiException.conflict("DUPLICATE_STUDENT_IDENTITY", "JMBG, email ili korisnicko ime vec postoji.");
        }
        StudijskiProgram program = programRepo.findById(app.getStudijskiProgramId())
                .orElseThrow(() -> ApiException.notFound("Studijski program ne postoji: " + app.getStudijskiProgramId()));
        StudentPodaci student = new StudentPodaci();
        student.setIme(app.getIme()); student.setPrezime(app.getPrezime()); student.setJmbg(app.getJmbg());
        student.setEmailFakultetski(app.getEmail());
        studentRepo.save(student);

        StudentIndeks indeks = new StudentIndeks();
        indeks.setStudent(student); indeks.setStudijskiProgram(program); indeks.setStudProgramOznaka(program.getOznaka());
        indeks.setGodina(app.getGodina()); indeks.setBroj(indeksService.allocateNextBrojForUpdate(app.getGodina(), program.getOznaka()));
        indeks.setAktivan(true); indeks.setStatus(StudentStatus.AKTIVAN); indeks.setVaziOd(LocalDate.now());
        indeks.setNacinFinansiranja(FinancingType.SELF_FINANCED.name());
        indeksRepo.save(indeks);

        UserAccount user = new UserAccount();
        user.setUsername(app.getUsername()); user.setPasswordHash(passwordEncoder.encode(initialPassword));
        user.setRole(Role.STUDENT); user.setEnabled(true); user.setMustChangePassword(true); user.setLinkedStudentPodaci(student); user.setLinkedStudentIndeks(indeks);
        userRepo.save(user);
        tuitionService.createInitialPlan(indeks, FinancingType.SELF_FINANCED, app.getTuitionEur());

        app.setCreatedStudent(student); app.setCreatedIndeks(indeks); app.setStatus(EnrollmentApplication.Status.APPROVED);
        decide(app, "APPROVED", null);
        return applicationRepo.save(app);
    }

    public EnrollmentApplication reject(Long id, String reason) {
        permissions.require(Permission.ENROLLMENT_WRITE);
        EnrollmentApplication app = require(id);
        if (app.getStatus() != EnrollmentApplication.Status.SUBMITTED) throw ApiException.conflict("ENROLLMENT_ALREADY_DECIDED", "Prijava je vec obradjena.");
        app.setStatus(EnrollmentApplication.Status.REJECTED);
        decide(app, "REJECTED", reason);
        return applicationRepo.save(app);
    }

    @Transactional(readOnly = true)
    public EnrollmentApplication require(Long id) {
        permissions.require(Permission.ENROLLMENT_WRITE);
        return applicationRepo.findById(id).orElseThrow(() -> ApiException.notFound("Upisna prijava ne postoji: " + id));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentApplication> list() {
        permissions.require(Permission.ENROLLMENT_WRITE);
        return applicationRepo.findAll();
    }

    private void decide(EnrollmentApplication app, String decision, String reason) {
        app.setDecisionReason(reason); app.setDecidedAt(LocalDateTime.now()); app.setDecidedByUserId(currentUser.userId());
        EnrollmentDecision item = new EnrollmentDecision();
        item.setApplication(app); item.setDecision(decision); item.setReason(reason); item.setActorUserId(currentUser.userId());
        decisionRepo.save(item);
    }
}
