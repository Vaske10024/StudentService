package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.*;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequest;
import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequestHistory;
import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentTransferredSubject;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.enrollment.StudyYearEnrollmentRequestHistoryRepository;
import org.raflab.studsluzba.repositories.enrollment.StudyYearEnrollmentRequestRepository;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.utils.EntityMappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudyYearEnrollmentService {

    private static final Set<StudyYearEnrollmentRequest.Status> ACTIVE_STATUSES =
            EnumSet.of(StudyYearEnrollmentRequest.Status.SUBMITTED,
                    StudyYearEnrollmentRequest.Status.PENDING_DOCUMENTS,
                    StudyYearEnrollmentRequest.Status.PENDING_ADMIN_APPROVAL,
                    StudyYearEnrollmentRequest.Status.NEEDS_CHANGES);

    private final StudyYearEnrollmentRequestRepository requestRepo;
    private final StudyYearEnrollmentRequestHistoryRepository historyRepo;
    private final StudentIndeksRepository indeksRepo;
    private final UpisGodineRepository upisRepo;
    private final ObnovaGodineRepository obnovaRepo;
    private final SkolskaGodinaRepository schoolYearRepo;
    private final SlusaPredmetRepository slusaRepo;
    private final ProgramPredmetRepository programSubjectRepo;
    private final IspitQueryRepository examRepo;
    private final AuditLogRepository auditRepo;
    private final AcademicProgressService academicProgressService;
    private final StudentIspitiViewService studentIspitiViewService;
    private final ECTSRuleService ectsRuleService;
    private final StudyYearEnrollmentPolicy policy;
    private final RealizacijaPredmetaService realizacijaService;
    private final SlusaPredmetService slusaPredmetService;
    private final NotificationService notificationService;
    private final PermissionService permissions;
    private final CurrentUser currentUser;
    private final EntityMappers mappers;

    @Transactional(readOnly = true)
    public StudyYearEnrollmentEligibilityDTO myEligibility() {
        return eligibility(requireStudentIndeks());
    }

    @Transactional(readOnly = true)
    public List<StudyYearEnrollmentRequestDTO> myRequests() {
        Long indeksId = requireStudentIndeks().getId();
        return requestRepo.findByStudentIndeksIdOrderBySubmittedAtDesc(indeksId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public StudyYearEnrollmentRequestDTO submit(StudyYearEnrollmentRequestCreateDTO dto) {
        StudentIndeks linked = requireStudentIndeks();
        StudentIndeks indeks = indeksRepo.findByIdForUpdate(linked.getId());
        if (indeks == null) throw ApiException.notFound("Indeks ne postoji.");

        StudyYearEnrollmentEligibilityDTO eligibility = eligibility(indeks);
        if (!eligibility.isCanSubmit() || eligibility.getTargetSchoolYear() == null) {
            throw ApiException.conflict("YEAR_ENROLLMENT_NOT_AVAILABLE", eligibility.getMessage());
        }
        if (!eligibility.getTargetSchoolYear().getId().equals(dto.getTargetSchoolYearId())) {
            throw ApiException.conflict("TARGET_SCHOOL_YEAR_CHANGED",
                    "Ciljna skolska godina vise nije vazeca. Osvezite eligibility podatke.");
        }

        StudyYearEnrollmentRequest.Type type = parseType(dto.getType());
        if (!type.name().equals(eligibility.getSuggestedType())) {
            throw ApiException.conflict("INVALID_ENROLLMENT_REQUEST_TYPE",
                    "Tip zahteva mora odgovarati trenutno izracunatom uslovu.");
        }
        if (requestRepo.existsByStudentIndeksIdAndTargetSchoolYearIdAndStatusIn(
                indeks.getId(), dto.getTargetSchoolYearId(), ACTIVE_STATUSES)) {
            throw ApiException.conflict("DUPLICATE_ACTIVE_YEAR_REQUEST",
                    "Vec postoji aktivan zahtev za ciljnu skolsku godinu.");
        }

        StudyYearEnrollmentRequest request = new StudyYearEnrollmentRequest();
        request.setStudentIndeks(indeks);
        request.setCurrentSchoolYear(requireSchoolYear(eligibility.getCurrentSchoolYear().getId()));
        request.setTargetSchoolYear(requireSchoolYear(dto.getTargetSchoolYearId()));
        request.setType(type);
        request.setStatus(StudyYearEnrollmentRequest.Status.PENDING_DOCUMENTS);
        request.setCurrentStudyYear(eligibility.getCurrentStudyYear());
        request.setRequestedStudyYear(eligibility.getRequestedStudyYear());
        request.setEarnedEctsSnapshot(eligibility.getEarnedEcts());
        request.setStudentNote(trimToNull(dto.getStudentNote()));
        request.setSubmittedByUserId(currentUser.userId());
        replaceTransferredSubjects(request, dto.getTransferredSubjectIds());
        assertSelectionRules(request);
        request = requestRepo.save(request);
        writeHistory(request, null, request.getStatus(),
                "Zahtev je podnet. Student treba da donese ugovor, dokumentaciju i potvrdu uplate.");
        audit("YEAR_ENROLLMENT_SUBMITTED", request);
        notificationService.notifyStudent(indeks.getId(), "YEAR_ENROLLMENT_SUBMITTED",
                "Zahtev za upis godine je podnet",
                "Zahtev ceka ugovor, dokumentaciju i potvrdu uplate u studentskoj sluzbi.");
        return toDto(request);
    }

    public StudyYearEnrollmentRequestDTO updateAndResubmit(Long requestId, StudyYearEnrollmentRequestUpdateDTO dto) {
        StudyYearEnrollmentRequest request = requireOwnedRequest(requestId);
        if (request.getStatus() != StudyYearEnrollmentRequest.Status.NEEDS_CHANGES
                && request.getStatus() != StudyYearEnrollmentRequest.Status.PENDING_DOCUMENTS) {
            throw ApiException.conflict("YEAR_REQUEST_NOT_EDITABLE",
                    "Zahtev se moze menjati samo dok ceka dokumentaciju ili dopunu.");
        }
        replaceTransferredSubjects(request, dto.getTransferredSubjectIds());
        assertSelectionRules(request);
        request.setStudentNote(trimToNull(dto.getStudentNote()));
        request.setAdminNote(null);
        StudyYearEnrollmentRequest.Status next = allDocumentsConfirmed(request)
                ? StudyYearEnrollmentRequest.Status.PENDING_ADMIN_APPROVAL
                : StudyYearEnrollmentRequest.Status.PENDING_DOCUMENTS;
        transition(request, next, "Student je dopunio i ponovo poslao zahtev.");
        notificationService.notifyStudent(request.getStudentIndeks().getId(), "YEAR_ENROLLMENT_RESUBMITTED",
                "Zahtev je ponovo poslat", "Dopunjeni zahtev je prosledjen studentskoj sluzbi.");
        return toDto(requestRepo.save(request));
    }

    public StudyYearEnrollmentRequestDTO cancel(Long requestId) {
        StudyYearEnrollmentRequest request = requireOwnedRequest(requestId);
        if (request.getStatus() == StudyYearEnrollmentRequest.Status.APPROVED
                || request.getStatus() == StudyYearEnrollmentRequest.Status.REJECTED
                || request.getStatus() == StudyYearEnrollmentRequest.Status.CANCELLED) {
            throw ApiException.conflict("YEAR_REQUEST_CANNOT_BE_CANCELLED",
                    "Obradjen zahtev se ne moze otkazati.");
        }
        transition(request, StudyYearEnrollmentRequest.Status.CANCELLED, "Student je otkazao zahtev.");
        audit("YEAR_ENROLLMENT_CANCELLED", request);
        return toDto(requestRepo.save(request));
    }

    @Transactional(readOnly = true)
    public List<StudyYearEnrollmentRequestDTO> adminList(StudyYearEnrollmentRequest.Status status,
                                                         StudyYearEnrollmentRequest.Type type,
                                                         Long targetSchoolYearId,
                                                         Long studentIndeksId) {
        requireAdminPermission();
        return requestRepo.search(status, type, targetSchoolYearId, studentIndeksId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public StudyYearEnrollmentRequestDTO adminDetail(Long requestId) {
        requireAdminPermission();
        return toDto(requireRequest(requestId));
    }

    public StudyYearEnrollmentRequestDTO updateChecklist(Long requestId, StudyYearEnrollmentChecklistDTO dto) {
        requireAdminPermission();
        StudyYearEnrollmentRequest request = requireOpenAdminRequest(requestId);
        request.setContractReceived(dto.isContractReceived());
        request.setPaymentConfirmed(dto.isPaymentConfirmed());
        request.setDocumentationComplete(dto.isDocumentationComplete());
        request.setAdminNote(trimToNull(dto.getNote()));
        StudyYearEnrollmentRequest.Status next = allDocumentsConfirmed(request)
                ? StudyYearEnrollmentRequest.Status.PENDING_ADMIN_APPROVAL
                : StudyYearEnrollmentRequest.Status.PENDING_DOCUMENTS;
        transition(request, next, dto.getNote());
        audit("YEAR_ENROLLMENT_CHECKLIST_UPDATED", request);
        notificationService.notifyStudent(request.getStudentIndeks().getId(), "YEAR_ENROLLMENT_CHECKLIST_UPDATED",
                "Dokumentacija za upis je azurirana",
                allDocumentsConfirmed(request)
                        ? "Dokumentacija je kompletna i zahtev ceka odobrenje."
                        : "Nedostaje deo dokumentacije, ugovor ili potvrda uplate.");
        return toDto(requestRepo.save(request));
    }

    public StudyYearEnrollmentRequestDTO approve(Long requestId) {
        requireAdminPermission();
        StudyYearEnrollmentRequest request = requireRequest(requestId);
        if (request.getStatus() != StudyYearEnrollmentRequest.Status.PENDING_ADMIN_APPROVAL) {
            throw ApiException.conflict("YEAR_REQUEST_NOT_READY",
                    "Samo zahtev koji ceka admin odobrenje moze biti odobren.");
        }
        if (!allDocumentsConfirmed(request)) {
            throw ApiException.conflict("YEAR_REQUEST_DOCUMENTS_MISSING",
                    "Ugovor, dokumentacija i uplata moraju biti potvrdjeni pre odobrenja.");
        }

        StudentIndeks indeks = indeksRepo.findByIdForUpdate(request.getStudentIndeks().getId());
        if (indeks == null) throw ApiException.notFound("Indeks ne postoji.");
        if (upisRepo.existsByIndeksIdAndUpisujeGodinuAndSkolskaGodinaId(
                indeks.getId(), request.getRequestedStudyYear(), request.getTargetSchoolYear().getId())) {
            throw ApiException.conflict("YEAR_ALREADY_ENROLLED",
                    "Student je vec upisan u trazenu godinu i skolsku godinu.");
        }

        validateTransferredSubjects(indeks, transferredSubjectIds(request));
        int earnedEcts = academicProgressService.recalculateEarnedEcts(indeks.getId());
        assertRequestStillEligible(request, earnedEcts);

        UpisGodine enrollment = new UpisGodine();
        enrollment.setIndeks(indeks);
        enrollment.setUpisujeGodinu(request.getRequestedStudyYear());
        enrollment.setDatum(LocalDate.now());
        enrollment.setNapomena("Odobren zahtev #" + request.getId() + " (" + request.getType().name() + ")");
        enrollment.setSkolskaGodina(request.getTargetSchoolYear());
        enrollment = upisRepo.save(enrollment);

        enrollSubjectsForApprovedRequest(request, indeks, enrollment);

        if (request.getType() == StudyYearEnrollmentRequest.Type.RENEW_YEAR) {
            ObnovaGodine renewal = new ObnovaGodine();
            renewal.setIndeks(indeks);
            renewal.setObnavljaGodinu(request.getRequestedStudyYear());
            renewal.setDatum(LocalDate.now());
            renewal.setNapomena("Odobren zahtev #" + request.getId());
            renewal.setSkolskaGodina(request.getTargetSchoolYear());
            renewal.setUpisGodine(enrollment);
            request.setApprovedRenewal(obnovaRepo.save(renewal));
        }

        request.setApprovedEnrollment(enrollment);
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedByUserId(currentUser.userId());
        transition(request, StudyYearEnrollmentRequest.Status.APPROVED, "Admin je odobrio upis godine.");
        request = requestRepo.save(request);
        audit("YEAR_ENROLLMENT_APPROVED", request);
        notificationService.notifyStudent(indeks.getId(), "YEAR_ENROLLMENT_APPROVED",
                "Upis godine je odobren",
                "Upis u " + request.getRequestedStudyYear() + ". godinu za skolsku godinu "
                        + request.getTargetSchoolYear().getGodina() + " je evidentiran.");
        return toDto(request);
    }

    public StudyYearEnrollmentRequestDTO reject(Long requestId, String reason) {
        requireAdminPermission();
        StudyYearEnrollmentRequest request = requireOpenAdminRequest(requestId);
        request.setAdminNote(trimToNull(reason));
        request.setDecidedAt(LocalDateTime.now());
        request.setDecidedByUserId(currentUser.userId());
        transition(request, StudyYearEnrollmentRequest.Status.REJECTED, reason);
        audit("YEAR_ENROLLMENT_REJECTED", request);
        notificationService.notifyStudent(request.getStudentIndeks().getId(), "YEAR_ENROLLMENT_REJECTED",
                "Zahtev za upis godine je odbijen", reason);
        return toDto(requestRepo.save(request));
    }

    public StudyYearEnrollmentRequestDTO needsChanges(Long requestId, String reason) {
        requireAdminPermission();
        StudyYearEnrollmentRequest request = requireOpenAdminRequest(requestId);
        request.setAdminNote(trimToNull(reason));
        transition(request, StudyYearEnrollmentRequest.Status.NEEDS_CHANGES, reason);
        audit("YEAR_ENROLLMENT_NEEDS_CHANGES", request);
        notificationService.notifyStudent(request.getStudentIndeks().getId(), "YEAR_ENROLLMENT_NEEDS_CHANGES",
                "Zahtev za upis godine treba dopunu", reason);
        return toDto(requestRepo.save(request));
    }

    StudyYearEnrollmentEligibilityDTO eligibility(StudentIndeks indeks) {
        StudyYearEnrollmentEligibilityDTO dto = new StudyYearEnrollmentEligibilityDTO();
        dto.setIndeksId(indeks.getId());
        dto.setProgramDuration(programDuration(indeks));
        int earnedEcts = academicProgressService.calculateEarnedEcts(indeks.getId());
        dto.setEarnedEcts(earnedEcts);
        dto.setPassedSubjects(studentIspitiViewService.polozenePaged(
                indeks.getStudProgramOznaka(), indeks.getGodina(), indeks.getBroj(), 0, 1000).getContent());
        dto.setTransferableSubjects(transferableSubjects(indeks));

        UpisGodine currentEnrollment = latestEnrollment(indeks.getId());
        if (currentEnrollment == null || currentEnrollment.getSkolskaGodina() == null) {
            dto.setCanSubmit(false);
            dto.setMessage("Student nema postojeci upis godine iz kog moze pokrenuti sledeci zahtev.");
            return dto;
        }

        int currentStudyYear = currentEnrollment.getUpisujeGodinu();
        int fallbackRegular = policy.regularThresholdForCurrentYear(currentStudyYear);
        int regular = ectsRuleService.minimumEctsFor(indeks, currentStudyYear + 1, fallbackRegular);
        int conditional = Math.min(policy.conditionalThresholdForCurrentYear(currentStudyYear), Math.max(0, regular - 1));
        StudyYearEnrollmentRequest.Type suggested = policy.suggest(
                currentStudyYear, dto.getProgramDuration(), earnedEcts, regular, conditional);

        dto.setCurrentStudyYear(currentStudyYear);
        dto.setRequestedStudyYear(policy.requestedStudyYear(suggested, currentStudyYear));
        dto.setRegularEnrollmentThreshold(regular);
        dto.setConditionalEnrollmentThreshold(conditional);
        dto.setSuggestedType(suggested.name());
        dto.setCurrentSchoolYear(mappers.fromSkolskaGodinaToDTO(currentEnrollment.getSkolskaGodina()));

        SkolskaGodina target = nextSchoolYear(currentEnrollment.getSkolskaGodina());
        if (target == null) {
            dto.setCanSubmit(false);
            dto.setMessage("Sledeca skolska godina jos nije konfigurisana. Obratite se studentskoj sluzbi.");
            return dto;
        }
        dto.setTargetSchoolYear(mappers.fromSkolskaGodinaToDTO(target));
        boolean duplicate = requestRepo.existsByStudentIndeksIdAndTargetSchoolYearIdAndStatusIn(
                indeks.getId(), target.getId(), ACTIVE_STATUSES);
        dto.setCanSubmit(!duplicate);
        dto.setMessage(duplicate
                ? "Vec postoji aktivan zahtev za sledecu skolsku godinu."
                : eligibilityMessage(suggested, earnedEcts, regular, conditional));
        return dto;
    }

    private void enrollSubjectsForApprovedRequest(StudyYearEnrollmentRequest request, StudentIndeks indeks,
                                                   UpisGodine enrollment) {
        Long programId = Optional.ofNullable(indeks.getStudijskiProgram())
                .map(p -> p.getId())
                .orElseThrow(() -> ApiException.conflict("INDEX_WITHOUT_PROGRAM",
                        "Indeks nema vezan studijski program."));

        if (request.getType() != StudyYearEnrollmentRequest.Type.RENEW_YEAR) {
            List<RealizacijaPredmeta> standard = realizacijaService.ensureForEnrollment(
                    programId, request.getRequestedStudyYear(), request.getTargetSchoolYear().getId()).stream()
                    .filter(r -> !examRepo.existsPassedSubject(indeks.getId(),
                            r.getProgramPredmet().getPredmet().getId()))
                    .collect(Collectors.toList());
            slusaPredmetService.enrollRealizations(indeks, enrollment, standard);
        }

        for (StudyYearEnrollmentTransferredSubject selected : request.getTransferredSubjects()) {
            ProgramPredmet pp = programSubjectRepo.findByProgramIdAndPredmetId(programId, selected.getSubject().getId())
                    .orElseThrow(() -> ApiException.conflict("TRANSFER_SUBJECT_NOT_ON_PROGRAM",
                            "Izabrani preneti predmet vise nije na studijskom programu."));
            RealizacijaPredmeta realization = realizacijaService.ensure(pp, request.getTargetSchoolYear());
            slusaPredmetService.enroll(indeks, realization, enrollment);
        }
    }

    private void assertRequestStillEligible(StudyYearEnrollmentRequest request, int earnedEcts) {
        int regular = ectsRuleService.minimumEctsFor(request.getStudentIndeks(),
                request.getCurrentStudyYear() + 1,
                policy.regularThresholdForCurrentYear(request.getCurrentStudyYear()));
        int conditional = Math.min(policy.conditionalThresholdForCurrentYear(request.getCurrentStudyYear()),
                Math.max(0, regular - 1));
        if (request.getType() == StudyYearEnrollmentRequest.Type.ENROLL_NEXT_YEAR && earnedEcts < regular) {
            throw ApiException.conflict("ECTS_REQUIREMENT_NOT_MET",
                    "Student vise ne ispunjava ESPB uslov za redovan upis.");
        }
        if (request.getType() == StudyYearEnrollmentRequest.Type.CONDITIONAL_ENROLLMENT
                && earnedEcts < conditional) {
            throw ApiException.conflict("ECTS_REQUIREMENT_NOT_MET",
                    "Student vise ne ispunjava ESPB uslov za uslovni upis.");
        }
    }

    private void replaceTransferredSubjects(StudyYearEnrollmentRequest request, Collection<Long> subjectIds) {
        Set<Long> normalized = subjectIds == null ? Collections.emptySet() : subjectIds.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toCollection(LinkedHashSet::new));
        validateTransferredSubjects(request.getStudentIndeks(), normalized);
        Map<Long, Predmet> subjects = transferableSubjectEntities(request.getStudentIndeks()).stream()
                .collect(Collectors.toMap(Predmet::getId, Function.identity(), (a, b) -> a, LinkedHashMap::new));
        request.getTransferredSubjects().clear();
        for (Long subjectId : normalized) {
            Predmet subject = subjects.get(subjectId);
            if (subject == null) {
                throw ApiException.conflict("SUBJECT_NOT_TRANSFERABLE",
                        "Izabrani predmet nije ponudjen za prenos: " + subjectId);
            }
            StudyYearEnrollmentTransferredSubject selected = new StudyYearEnrollmentTransferredSubject();
            selected.setRequest(request);
            selected.setSubject(subject);
            selected.setEctsSnapshot(subject.getEspb() == null ? 0 : subject.getEspb());
            request.getTransferredSubjects().add(selected);
        }
    }

    private void validateTransferredSubjects(StudentIndeks indeks, Collection<Long> subjectIds) {
        int total = 0;
        Map<Long, Predmet> transferable = transferableSubjectEntities(indeks).stream()
                .collect(Collectors.toMap(Predmet::getId, Function.identity(), (a, b) -> a));
        for (Long subjectId : subjectIds) {
            if (examRepo.existsPassedSubject(indeks.getId(), subjectId)) {
                throw ApiException.conflict("PASSED_SUBJECT_CANNOT_BE_TRANSFERRED",
                        "Polozeni predmet se ne moze preneti: " + subjectId);
            }
            Predmet subject = transferable.get(subjectId);
            if (subject == null) {
                throw ApiException.conflict("SUBJECT_NOT_TRANSFERABLE",
                        "Predmet se ne moze preneti: " + subjectId);
            }
            total += subject.getEspb() == null ? 0 : subject.getEspb();
        }
        if (total > policy.getMaximumTransferredEcts()) {
            throw ApiException.conflict("TRANSFER_ECTS_LIMIT_EXCEEDED",
                    "Izabrani preneti predmeti prelaze dozvoljeni maksimum od "
                            + policy.getMaximumTransferredEcts() + " ESPB.");
        }
    }

    private void assertSelectionRules(StudyYearEnrollmentRequest request) {
        if (request.getType() == StudyYearEnrollmentRequest.Type.RENEW_YEAR
                && request.getTransferredSubjects().isEmpty()
                && !transferableSubjectEntities(request.getStudentIndeks()).isEmpty()) {
            throw ApiException.conflict("RENEWAL_SUBJECTS_REQUIRED",
                    "Za obnovu godine mora biti izabran najmanje jedan nepolozeni predmet.");
        }
    }

    private List<Predmet> transferableSubjectEntities(StudentIndeks indeks) {
        Map<Long, Predmet> result = new LinkedHashMap<>();
        for (SlusaPredmet listening : slusaRepo.findAllForStudent(indeks.getId())) {
            if (listening.getRealizacijaPredmeta() == null
                    || listening.getRealizacijaPredmeta().getProgramPredmet() == null) continue;
            Predmet subject = listening.getRealizacijaPredmeta().getProgramPredmet().getPredmet();
            if (subject != null && subject.getId() != null
                    && !examRepo.existsPassedSubject(indeks.getId(), subject.getId())) {
                result.putIfAbsent(subject.getId(), subject);
            }
        }
        return new ArrayList<>(result.values());
    }

    private List<PredmetDTO> transferableSubjects(StudentIndeks indeks) {
        return transferableSubjectEntities(indeks).stream()
                .map(mappers::fromPredmetToDTO)
                .collect(Collectors.toList());
    }

    private StudyYearEnrollmentRequest requireOwnedRequest(Long requestId) {
        StudyYearEnrollmentRequest request = requireRequest(requestId);
        currentUser.requireStudentOwnsIndeks(request.getStudentIndeks().getId());
        return request;
    }

    private StudyYearEnrollmentRequest requireOpenAdminRequest(Long requestId) {
        StudyYearEnrollmentRequest request = requireRequest(requestId);
        if (request.getStatus() == StudyYearEnrollmentRequest.Status.APPROVED
                || request.getStatus() == StudyYearEnrollmentRequest.Status.REJECTED
                || request.getStatus() == StudyYearEnrollmentRequest.Status.CANCELLED) {
            throw ApiException.conflict("YEAR_REQUEST_ALREADY_CLOSED", "Zahtev je vec zavrsen.");
        }
        return request;
    }

    private StudyYearEnrollmentRequest requireRequest(Long requestId) {
        return requestRepo.findById(requestId)
                .orElseThrow(() -> ApiException.notFound("Zahtev za upis godine ne postoji: " + requestId));
    }

    private StudentIndeks requireStudentIndeks() {
        if (currentUser.role() != Role.STUDENT || currentUser.linkedStudentIndeksId() == null) {
            throw ApiException.forbidden("Samo student sa povezanim indeksom moze koristiti ovaj tok.");
        }
        return indeksRepo.findById(currentUser.linkedStudentIndeksId())
                .orElseThrow(() -> ApiException.notFound("Povezani indeks ne postoji."));
    }

    private void requireAdminPermission() {
        currentUser.requireAdmin();
        permissions.require(Permission.ENROLLMENT_WRITE);
    }

    private StudyYearEnrollmentRequest.Type parseType(String value) {
        try {
            return StudyYearEnrollmentRequest.Type.valueOf(value);
        } catch (Exception ex) {
            throw ApiException.badRequest("Nepoznat tip zahteva za upis godine.");
        }
    }

    private SkolskaGodina requireSchoolYear(Long id) {
        return schoolYearRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Skolska godina ne postoji: " + id));
    }

    private UpisGodine latestEnrollment(Long indeksId) {
        List<UpisGodine> all = upisRepo.findUpisi(indeksId);
        return all.isEmpty() ? null : all.get(0);
    }

    private SkolskaGodina nextSchoolYear(SkolskaGodina current) {
        int currentStart = schoolYearStart(current.getGodina());
        SkolskaGodina result = null;
        int resultStart = Integer.MAX_VALUE;
        for (SkolskaGodina candidate : schoolYearRepo.findAll()) {
            int start = schoolYearStart(candidate.getGodina());
            if (start > currentStart && start < resultStart) {
                result = candidate;
                resultStart = start;
            }
        }
        return result;
    }

    private int schoolYearStart(String label) {
        if (label == null) return Integer.MIN_VALUE;
        String first = label.trim().split("[^0-9]+", 2)[0];
        try {
            int value = Integer.parseInt(first);
            return first.length() == 2 ? 2000 + value : value;
        } catch (NumberFormatException ex) {
            return Integer.MIN_VALUE;
        }
    }

    private int programDuration(StudentIndeks indeks) {
        if (indeks.getStudijskiProgram() == null || indeks.getStudijskiProgram().getTrajanjeGodina() == null) {
            return 4;
        }
        return indeks.getStudijskiProgram().getTrajanjeGodina();
    }

    private boolean allDocumentsConfirmed(StudyYearEnrollmentRequest request) {
        return request.isContractReceived() && request.isPaymentConfirmed() && request.isDocumentationComplete();
    }

    private Set<Long> transferredSubjectIds(StudyYearEnrollmentRequest request) {
        return request.getTransferredSubjects().stream()
                .map(item -> item.getSubject().getId())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String eligibilityMessage(StudyYearEnrollmentRequest.Type type, int earned, int regular, int conditional) {
        if (type == StudyYearEnrollmentRequest.Type.ENROLL_NEXT_YEAR) {
            return "Ispunjen je uslov za redovan upis naredne godine (" + earned + "/" + regular + " ESPB).";
        }
        if (type == StudyYearEnrollmentRequest.Type.CONDITIONAL_ENROLLMENT) {
            return "Ispunjen je uslov za uslovni upis (" + earned + "/" + conditional + " ESPB).";
        }
        return "Nije ispunjen uslov za prelazak u narednu godinu; predlaze se obnova.";
    }

    private void transition(StudyYearEnrollmentRequest request, StudyYearEnrollmentRequest.Status next, String note) {
        StudyYearEnrollmentRequest.Status old = request.getStatus();
        request.setStatus(next);
        if (old != next) writeHistory(request, old, next, note);
    }

    private void writeHistory(StudyYearEnrollmentRequest request, StudyYearEnrollmentRequest.Status oldStatus,
                              StudyYearEnrollmentRequest.Status newStatus, String note) {
        StudyYearEnrollmentRequestHistory history = new StudyYearEnrollmentRequestHistory();
        history.setRequest(request);
        history.setOldStatus(oldStatus);
        history.setNewStatus(newStatus);
        history.setNote(trimToNull(note));
        history.setActorUserId(currentUser.userId());
        historyRepo.save(history);
    }

    private void audit(String action, StudyYearEnrollmentRequest request) {
        AuditLog log = new AuditLog();
        log.setActorUserId(currentUser.userId());
        log.setAction(action);
        log.setDetails("requestId=" + request.getId() + ", indeksId=" + request.getStudentIndeks().getId()
                + ", status=" + request.getStatus());
        auditRepo.save(log);
    }

    private StudyYearEnrollmentRequestDTO toDto(StudyYearEnrollmentRequest request) {
        StudyYearEnrollmentRequestDTO dto = new StudyYearEnrollmentRequestDTO();
        dto.setId(request.getId());
        dto.setIndeksId(request.getStudentIndeks().getId());
        if (request.getStudentIndeks().getStudent() != null) {
            dto.setStudentName(request.getStudentIndeks().getStudent().getIme() + " "
                    + request.getStudentIndeks().getStudent().getPrezime());
        }
        dto.setIndexLabel(request.getStudentIndeks().getStudProgramOznaka() + " "
                + request.getStudentIndeks().getBroj() + "/" + request.getStudentIndeks().getGodina());
        dto.setType(request.getType().name());
        dto.setStatus(request.getStatus().name());
        dto.setCurrentStudyYear(request.getCurrentStudyYear());
        dto.setRequestedStudyYear(request.getRequestedStudyYear());
        dto.setEarnedEctsSnapshot(request.getEarnedEctsSnapshot());
        dto.setCurrentSchoolYear(mappers.fromSkolskaGodinaToDTO(request.getCurrentSchoolYear()));
        dto.setTargetSchoolYear(mappers.fromSkolskaGodinaToDTO(request.getTargetSchoolYear()));
        dto.setContractReceived(request.isContractReceived());
        dto.setPaymentConfirmed(request.isPaymentConfirmed());
        dto.setDocumentationComplete(request.isDocumentationComplete());
        dto.setStudentNote(request.getStudentNote());
        dto.setAdminNote(request.getAdminNote());
        dto.setSubmittedByUserId(request.getSubmittedByUserId());
        dto.setDecidedByUserId(request.getDecidedByUserId());
        dto.setApprovedEnrollmentId(request.getApprovedEnrollment() == null ? null : request.getApprovedEnrollment().getId());
        dto.setApprovedRenewalId(request.getApprovedRenewal() == null ? null : request.getApprovedRenewal().getId());
        dto.setSubmittedAt(request.getSubmittedAt());
        dto.setUpdatedAt(request.getUpdatedAt());
        dto.setDecidedAt(request.getDecidedAt());
        dto.setTransferredSubjects(request.getTransferredSubjects().stream()
                .map(item -> mappers.fromPredmetToDTO(item.getSubject()))
                .collect(Collectors.toList()));
        dto.setHistory(historyRepo.findByRequestIdOrderByCreatedAtAsc(request.getId()).stream()
                .map(item -> new StudyYearEnrollmentRequestHistoryDTO(item.getId(),
                        item.getOldStatus() == null ? null : item.getOldStatus().name(),
                        item.getNewStatus().name(), item.getNote(), item.getActorUserId(), item.getCreatedAt()))
                .collect(Collectors.toList()));
        return dto;
    }

    private String trimToNull(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        return value.trim();
    }
}
