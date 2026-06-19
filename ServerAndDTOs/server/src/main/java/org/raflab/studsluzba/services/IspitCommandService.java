package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.dtos.IspitIzlazakRequest;
import org.raflab.studsluzba.model.dtos.ExamEligibilityDTO;
import org.raflab.studsluzba.model.dtos.PrijavaCreateRequest;
import org.raflab.studsluzba.model.dtos.PrijavaResultUpdateRequest;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.ispiti.PrijavaStatus;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class IspitCommandService {

    private final IspitRepository ispitRepo;
    private final IspitQueryRepository iqRepo;
    private final StudentIndeksRepository siRepo;
    private final IspitQueryService iqService;
    private final SlusaPredmetRepository slusaRepo;
    private final PredmetRepository predmetRepo;
    private final UserAccountRepository userAccountRepo;
    private final GradingService gradingService;
    private final CurrentUser currentUser;
    private final StudentLifecycleService studentLifecycleService;
    private final AuditLogRepository auditLogRepo;
    private final DebtPolicyService debtPolicyService;
    private final PrerequisiteService prerequisiteService;
    private final NotificationService notificationService;
    private final AcademicProgressService academicProgressService;

    public Long prijaviStudenta(PrijavaCreateRequest req) {
        if (req == null) throw ApiException.badRequest("Request ne sme biti null.");

        Ispit ispit = ispitRepo.findById(req.getIspitId())
                .orElseThrow(() -> ApiException.notFound("Ispit ne postoji: " + req.getIspitId()));
        StudentIndeks si = siRepo.findById(req.getStudentIndeksId())
                .orElseThrow(() -> ApiException.notFound("StudentIndeks ne postoji: " + req.getStudentIndeksId()));

        if (ispit.isZakljucen()) {
            throw ApiException.conflict("Ispit je zaključan i prijava nije dozvoljena.");
        }
        studentLifecycleService.assertAcademicallyActive(si.getId());
        debtPolicyService.assertExamRegistrationAllowed(si.getId());
        ensureStudentAccountEnabled(si);

        Long predmetId = resolvePredmetId(ispit);
        if (predmetId == null) {
            throw ApiException.conflict("Ispit nema povezan predmet.");
        }
        prerequisiteService.assertSatisfied(si.getId(), predmetId);

        boolean slusa = slusaRepo.existsStudentSlusaPredmetAktivna(si.getId(), predmetId);
        if (!slusa) {
            throw ApiException.conflict("Student nema evidentirano slušanje ovog predmeta u aktivnoj školskoj godini.");
        }

        if (iqRepo.existsPassedSubject(si.getId(), predmetId)) {
            throw ApiException.conflict("Student je već položio ili priznao ovaj predmet.");
        }

        iqRepo.findAktivnaPrijava(ispit.getId(), si.getId()).ifPresent(p -> {
            throw ApiException.conflict("Student je već aktivno prijavljen na ovaj ispit.");
        });

        validateRegistrationWindow(ispit);

        PrijavaIspita pi = new PrijavaIspita();
        pi.setIspit(ispit);
        pi.setStudent(si);
        pi.setDatumPrijave(LocalDate.now());
        pi.setNapomena(req.getNapomena());
        pi.setPonisteno(false);
        pi.setDaLiJeIzasao(false);
        pi.setBrojOsvojenihPoena(0);
        pi.setOcena(5);
        pi.setStatus(PrijavaStatus.PRIJAVLJEN);

        PrijavaIspita saved = iqRepo.save(pi);
        return saved != null && saved.getId() != null ? saved.getId() : pi.getId();
    }

    @Transactional(readOnly = true)
    public ExamEligibilityDTO eligibility(Long ispitId, Long studentIndeksId) {
        try {
            Ispit ispit = ispitRepo.findById(ispitId)
                    .orElseThrow(() -> ApiException.notFound("Ispit ne postoji: " + ispitId));
            StudentIndeks si = siRepo.findById(studentIndeksId)
                    .orElseThrow(() -> ApiException.notFound("Indeks ne postoji: " + studentIndeksId));
            if (ispit.isZakljucen()) throw ApiException.conflict("EXAM_LOCKED", "Ispit je zaključan.");
            studentLifecycleService.assertAcademicallyActive(si.getId());
            debtPolicyService.assertExamRegistrationAllowed(si.getId());
            ensureStudentAccountEnabled(si);
            Long predmetId = resolvePredmetId(ispit);
            if (predmetId == null) throw ApiException.conflict("EXAM_WITHOUT_SUBJECT", "Ispit nema povezan predmet.");
            prerequisiteService.assertSatisfied(si.getId(), predmetId);
            if (!slusaRepo.existsStudentSlusaPredmetAktivna(si.getId(), predmetId)) {
                throw ApiException.conflict("SUBJECT_NOT_ENROLLED", "Student ne sluša predmet u aktivnoj školskoj godini.");
            }
            if (iqRepo.existsPassedSubject(si.getId(), predmetId)) {
                throw ApiException.conflict("SUBJECT_ALREADY_PASSED", "Predmet je već položen ili priznat.");
            }
            if (iqRepo.findAktivnaPrijava(ispitId, studentIndeksId).isPresent()) {
                throw ApiException.conflict("ALREADY_REGISTERED", "Ispit je već prijavljen.");
            }
            validateRegistrationWindow(ispit);
            return new ExamEligibilityDTO(true, "ELIGIBLE", "Ispit je dostupan za prijavu.");
        } catch (ApiException ex) {
            return new ExamEligibilityDTO(false, ex.getCode(), ex.getMessage());
        }
    }

    public Long azurirajRezultat(PrijavaResultUpdateRequest req) {
        if (req == null) throw ApiException.badRequest("Request ne sme biti null.");

        PrijavaIspita pi = iqRepo.findById(req.getPrijavaId())
                .orElseThrow(() -> ApiException.notFound("Prijava ne postoji: " + req.getPrijavaId()));
        ensurePrijavaCanBeModified(pi, "rezultat se ne može menjati");

        if (req.getBrojOsvojenihPoena() != null) {
            gradingService.validateExamPoints(req.getBrojOsvojenihPoena());
            pi.setBrojOsvojenihPoena(req.getBrojOsvojenihPoena());
        }
        if (req.getIzasao() != null) {
            pi.setDaLiJeIzasao(req.getIzasao());
        }
        if (req.getNapomena() != null) {
            pi.setNapomena(req.getNapomena());
        }

        int ukupno = iqService.ukupniPoeniZaPrijavu(pi);
        gradingService.validateTotalPoints(ukupno);

        if (req.getOcena() != null) {
            validateOcena(req.getOcena(), true);
            pi.setOcena(req.getOcena());
            gradingService.auditManualOverride(pi.getId(), req.getOcena(), pi.getBrojOsvojenihPoena());
        } else {
            pi.setOcena(gradingService.gradeForTotalPoints(ukupno));
        }

        PrijavaIspita saved = iqRepo.save(pi);
        notificationService.notifyStudent(pi.getStudent().getId(), "EXAM_RESULT", "Objavljen rezultat ispita",
                "Rezultat ispita je evidentiran. Ocena: " + pi.getOcena());
        return saved != null && saved.getId() != null ? saved.getId() : pi.getId();
    }

    public void ponisti(Long prijavaId, String reason) {
        requireReason(reason);
        PrijavaIspita pi = iqRepo.findById(prijavaId)
                .orElseThrow(() -> ApiException.notFound("Prijava ne postoji: " + prijavaId));
        ensurePrijavaCanBeModified(pi, "prijava se ne može poništiti");
        if (Boolean.TRUE.equals(pi.getPonisteno())) return;
        pi.setPonisteno(true);
        pi.setStatus(PrijavaStatus.PONISTEN);
        markCancellation(pi, reason);
        audit("EXAM_REGISTRATION_VOIDED", pi, reason);
        iqRepo.save(pi);
    }

    public void odjavi(Long prijavaId, String reason) {
        requireReason(reason);
        PrijavaIspita pi = iqRepo.findById(prijavaId)
                .orElseThrow(() -> ApiException.notFound("Prijava ne postoji: " + prijavaId));
        currentUser.requireStudentOwnsIndeks(pi.getStudent().getId());
        if (pi.getStatus() != PrijavaStatus.PRIJAVLJEN || Boolean.TRUE.equals(pi.getPonisteno())) {
            throw ApiException.conflict("REGISTRATION_NOT_ACTIVE", "Prijava nije aktivna.");
        }
        validateCancellationWindow(pi);
        pi.setStatus(PrijavaStatus.ODJAVLJEN);
        markCancellation(pi, reason);
        audit("EXAM_REGISTRATION_CANCELLED", pi, reason);
        iqRepo.save(pi);
    }

    public Long priznajPredmet(Long studentIndeksId, Long predmetId, Integer ocena, String napomena) {
        validateOcena(ocena, false);
        StudentIndeks si = siRepo.findById(studentIndeksId)
                .orElseThrow(() -> ApiException.notFound("StudentIndeks ne postoji: " + studentIndeksId));
        Predmet predmetEntity = predmetRepo.findById(predmetId)
                .orElseThrow(() -> ApiException.notFound("Nepostojeći predmet: " + predmetId));

        if (iqRepo.existsPassedSubject(studentIndeksId, predmetId)) {
            throw ApiException.conflict("Student već ima položen ili priznat predmet.");
        }
        if (iqRepo.existsRecognizedSubject(studentIndeksId, predmetId)) {
            throw ApiException.conflict("Predmet je već priznat za ovaj indeks.");
        }

        PrijavaIspita pi = new PrijavaIspita();
        pi.setStudent(si);
        pi.setDatumPrijave(LocalDate.now());
        pi.setNapomena(napomena);
        pi.setPonisteno(false);
        pi.setDaLiJeIzasao(false);
        pi.setBrojOsvojenihPoena(0);
        pi.setOcena(ocena);
        pi.setStatus(PrijavaStatus.PRIZNAT);
        pi.setPriznatSDrugogFakulteta(true);
        pi.setIspit(null);
        pi.setPredmet(predmetEntity);

        PrijavaIspita saved = iqRepo.save(pi);
        academicProgressService.recalculateEarnedEcts(studentIndeksId);
        return saved != null && saved.getId() != null ? saved.getId() : pi.getId();
    }

    public Long evidentirajIzlazak(IspitIzlazakRequest req) {
        if (req == null) throw ApiException.badRequest("Request ne sme biti null.");
        gradingService.validateExamPoints(req.getBrojOsvojenihPoena());

        PrijavaIspita pi = iqRepo.findById(req.getPrijavaId())
                .orElseThrow(() -> ApiException.notFound("Prijava ne postoji: " + req.getPrijavaId()));
        ensurePrijavaCanBeModified(pi, "izlazak se ne može evidentirati");

        pi.setDaLiJeIzasao(true);
        pi.setBrojOsvojenihPoena(req.getBrojOsvojenihPoena());
        if (req.getNapomena() != null) {
            pi.setNapomena(req.getNapomena());
        }

        int ukupno = iqService.ukupniPoeniZaPrijavu(pi);
        gradingService.validateTotalPoints(ukupno);
        pi.setOcena(gradingService.gradeForTotalPoints(ukupno));

        PrijavaIspita saved = iqRepo.save(pi);
        return saved != null && saved.getId() != null ? saved.getId() : pi.getId();
    }

    private void ensurePrijavaCanBeModified(PrijavaIspita pi, String action) {
        if (Boolean.TRUE.equals(pi.getPonisteno())) {
            throw ApiException.conflict("Prijava je poništena – " + action + ".");
        }
        if (pi.getIspit() != null && pi.getIspit().isZakljucen() && !currentUser.isAdmin()) {
            throw ApiException.conflict("Ispit je zaključan – " + action + ".");
        }
    }

    private void ensureStudentAccountEnabled(StudentIndeks si) {
        userAccountRepo.findByLinkedStudentIndeksId(si.getId()).ifPresent(ua -> {
            if (!ua.isEnabled()) throw ApiException.conflict("Studentov nalog nije aktivan.");
        });
        if (si.getStudent() != null) {
            userAccountRepo.findStudentAccountByStudentPodaciId(si.getStudent().getId()).ifPresent(ua -> {
                if (!ua.isEnabled()) throw ApiException.conflict("Studentov nalog nije aktivan.");
            });
        }
    }

    private Long resolvePredmetId(Ispit ispit) {
        if (ispit.getDrziPredmet() != null && ispit.getDrziPredmet().getPredmet() != null) {
            return ispit.getDrziPredmet().getPredmet().getId();
        }
        if (ispit.getPredmet() != null) {
            return ispit.getPredmet().getId();
        }
        return null;
    }

    private void validateRegistrationWindow(Ispit ispit) {
        LocalDateTime registrationStart = effectiveRegistrationStart(ispit);
        LocalDateTime registrationEnd = effectiveRegistrationEnd(ispit);
        if (ispit.getIspitniRok() == null || !ispit.getIspitniRok().isActive()
                || registrationStart == null || registrationEnd == null) {
            throw ApiException.conflict("REGISTRATION_WINDOW_NOT_CONFIGURED",
                    "Prozor za prijavu ispita nije konfigurisan.");
        }
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(registrationStart)) {
            throw ApiException.conflict("REGISTRATION_NOT_OPEN", "Prijava ispita jos nije otvorena.");
        }
        if (now.isAfter(registrationEnd)) {
            throw ApiException.conflict("REGISTRATION_CLOSED", "Rok za prijavu ispita je istekao.");
        }
    }

    private void validateCancellationWindow(PrijavaIspita pi) {
        LocalDateTime cancellationEnd = pi.getIspit() == null ? null : effectiveCancellationEnd(pi.getIspit());
        if (pi.getIspit() == null || pi.getIspit().getIspitniRok() == null || cancellationEnd == null) {
            throw ApiException.conflict("CANCELLATION_WINDOW_NOT_CONFIGURED",
                    "Prozor za odjavu ispita nije konfigurisan.");
        }
        if (LocalDateTime.now().isAfter(cancellationEnd)) {
            throw ApiException.conflict("CANCELLATION_CLOSED", "Rok za odjavu ispita je istekao.");
        }
    }

    private LocalDateTime effectiveRegistrationStart(Ispit ispit) {
        if (ispit.getRegistrationStart() != null) return ispit.getRegistrationStart();
        return ispit.getIspitniRok() == null ? null : ispit.getIspitniRok().getRegistrationStart();
    }

    private LocalDateTime effectiveRegistrationEnd(Ispit ispit) {
        if (ispit.getRegistrationEnd() != null) return ispit.getRegistrationEnd();
        return ispit.getIspitniRok() == null ? null : ispit.getIspitniRok().getRegistrationEnd();
    }

    private LocalDateTime effectiveCancellationEnd(Ispit ispit) {
        if (ispit.getCancellationEnd() != null) return ispit.getCancellationEnd();
        return ispit.getIspitniRok() == null ? null : ispit.getIspitniRok().getCancellationEnd();
    }

    private void requireReason(String reason) {
        if (reason == null || reason.trim().isEmpty()) {
            throw ApiException.badRequest("Razlog je obavezan.");
        }
    }

    private void markCancellation(PrijavaIspita pi, String reason) {
        pi.setCancelledAt(LocalDateTime.now());
        pi.setCancelledByUserId(currentUser.userId());
        pi.setCancellationReason(reason.trim());
    }

    private void audit(String action, PrijavaIspita pi, String reason) {
        AuditLog audit = new AuditLog();
        audit.setActorUserId(currentUser.userId());
        audit.setAction(action);
        audit.setDetails("prijavaId=" + pi.getId() + ", reason=" + reason.trim());
        auditLogRepo.save(audit);
    }

    private void validateLegacyRegistrationDeadline(Ispit ispit) {
        LocalDate today = LocalDate.now();
        if (ispit.getDatumOdrzavanja() != null && today.isAfter(ispit.getDatumOdrzavanja())) {
            throw ApiException.conflict("Rok za prijavu je istekao jer je datum ispita prošao.");
        }
        if (ispit.getIspitniRok() != null && ispit.getIspitniRok().getDatumZavrsetka() != null
                && today.isAfter(ispit.getIspitniRok().getDatumZavrsetka())) {
            throw ApiException.conflict("Ispitni rok je završen.");
        }
    }

    private void validateOcena(Integer ocena, boolean allowFive) {
        int min = allowFive ? 5 : 6;
        if (ocena == null || ocena < min || ocena > 10) {
            throw ApiException.badRequest("Ocena mora biti u opsegu [" + min + "..10].");
        }
    }
}
