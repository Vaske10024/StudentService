package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.*;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.StudentStatusHistoryRepository;
import org.raflab.studsluzba.repositories.StudentStatusRequestRepository;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StudentLifecycleService {
    private final StudentIndeksRepository indeksRepo;
    private final StudentStatusHistoryRepository historyRepo;
    private final StudentStatusRequestRepository requestRepo;
    private final AuditLogRepository auditRepo;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public StudentStatusDTO getStatus(Long indeksId) {
        return toStatus(requireIndeks(indeksId));
    }

    @Transactional(readOnly = true)
    public List<StudentStatusHistoryDTO> history(Long indeksId) {
        requireIndeks(indeksId);
        return historyRepo.findByStudentIndeksIdOrderByCreatedAtDesc(indeksId).stream()
                .map(this::toHistory)
                .collect(Collectors.toList());
    }

    public StudentStatusDTO changeStatus(Long indeksId, StudentStatusChangeRequest request) {
        return changeStatus(indeksId, request, false);
    }

    public StudentStatusDTO markGraduated(Long indeksId, String reason) {
        StudentStatusChangeRequest request = new StudentStatusChangeRequest();
        request.setNewStatus("DIPLOMIRAO");
        request.setReason(reason);
        return changeStatus(indeksId, request, true);
    }

    private StudentStatusDTO changeStatus(Long indeksId, StudentStatusChangeRequest request, boolean graduationWorkflow) {
        if (request == null) throw ApiException.badRequest("Request ne sme biti null.");
        StudentStatus target = parseStatus(request.getNewStatus());
        if (target == StudentStatus.DIPLOMIRAO && !graduationWorkflow) {
            throw ApiException.conflict("GRADUATION_WORKFLOW_REQUIRED", "Status DIPLOMIRAO se postavlja samo kroz graduation workflow.");
        }
        StudentIndeks indeks = requireIndeks(indeksId);
        StudentStatus old = effectiveStatus(indeks);
        validateTransition(old, target, request);
        if (old == target) throw ApiException.conflict("STUDENT_STATUS_UNCHANGED", "Student vec ima trazeni status.");

        indeks.setStatus(target);
        indeks.setStatusReason(request.getReason());
        indeks.setAktivan(target == StudentStatus.AKTIVAN);
        LocalDateTime now = LocalDateTime.now();
        if (target == StudentStatus.AKTIVAN) {
            indeks.setActivatedAt(now);
            indeks.setDeactivatedAt(null);
        } else {
            indeks.setDeactivatedAt(now);
        }
        indeksRepo.save(indeks);

        StudentStatusHistory history = new StudentStatusHistory();
        history.setStudentIndeks(indeks);
        history.setOldStatus(old);
        history.setNewStatus(target);
        history.setReason(request.getReason());
        history.setValidFrom(request.getValidFrom() == null ? LocalDate.now() : request.getValidFrom());
        history.setValidTo(request.getValidTo());
        history.setChangedByUserId(currentUser.userId());
        historyRepo.save(history);

        AuditLog audit = new AuditLog();
        audit.setActorUserId(currentUser.userId());
        audit.setAction("STUDENT_STATUS_CHANGED");
        audit.setDetails("indeksId=" + indeksId + ", old=" + old + ", new=" + target + ", reason=" + request.getReason());
        auditRepo.save(audit);
        return toStatus(indeks);
    }

    public void assertAcademicallyActive(Long indeksId) {
        StudentStatus status = effectiveStatus(requireIndeks(indeksId));
        if (status != StudentStatus.AKTIVAN) {
            throw ApiException.conflict("STUDENT_NOT_ACADEMICALLY_ACTIVE",
                    "Prijava ispita je dozvoljena samo studentu sa statusom AKTIVAN.");
        }
    }

    public StudentStatusRequestDTO createRequest(StudentStatusRequestCreateDTO dto) {
        if (dto == null) throw ApiException.badRequest("Request ne sme biti null.");
        currentUser.requireAdminOrStudentOwnsIndeks(dto.getIndeksId());
        StudentStatusRequest.Type type;
        try {
            type = StudentStatusRequest.Type.valueOf(dto.getType().trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw ApiException.badRequest("Dozvoljeni tipovi zahteva su MIROVANJE i ISPIS.");
        }
        if (type == StudentStatusRequest.Type.MIROVANJE
                && (dto.getRequestedFrom() == null || dto.getRequestedTo() == null
                || dto.getRequestedTo().isBefore(dto.getRequestedFrom()))) {
            throw ApiException.badRequest("Mirovanje zahteva validan period od-do.");
        }
        StudentStatusRequest request = new StudentStatusRequest();
        request.setStudentIndeks(requireIndeks(dto.getIndeksId()));
        request.setType(type);
        request.setReason(dto.getReason());
        request.setRequestedFrom(dto.getRequestedFrom());
        request.setRequestedTo(dto.getRequestedTo());
        request.setSubmittedByUserId(currentUser.userId());
        return toRequest(requestRepo.save(request));
    }

    @Transactional(readOnly = true)
    public List<StudentStatusRequestDTO> requests(Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return requestRepo.findByStudentIndeksIdOrderByCreatedAtDesc(indeksId).stream()
                .map(this::toRequest).collect(Collectors.toList());
    }

    public StudentStatusRequestDTO approveRequest(Long requestId, String decisionNote) {
        currentUser.requireAdmin();
        StudentStatusRequest request = requirePendingRequest(requestId);
        StudentStatusChangeRequest change = new StudentStatusChangeRequest();
        change.setNewStatus(request.getType() == StudentStatusRequest.Type.MIROVANJE ? "MIROVANJE" : "ISPISAN");
        change.setReason(request.getReason());
        change.setValidFrom(request.getRequestedFrom());
        change.setValidTo(request.getRequestedTo());
        changeStatus(request.getStudentIndeks().getId(), change);
        decide(request, StudentStatusRequest.Status.APPROVED, decisionNote);
        return toRequest(requestRepo.save(request));
    }

    public StudentStatusRequestDTO rejectRequest(Long requestId, String decisionNote) {
        currentUser.requireAdmin();
        StudentStatusRequest request = requirePendingRequest(requestId);
        decide(request, StudentStatusRequest.Status.REJECTED, decisionNote);
        return toRequest(requestRepo.save(request));
    }

    private void decide(StudentStatusRequest request, StudentStatusRequest.Status status, String note) {
        request.setStatus(status);
        request.setDecisionNote(note);
        request.setDecidedByUserId(currentUser.userId());
        request.setDecidedAt(LocalDateTime.now());
        AuditLog audit = new AuditLog();
        audit.setActorUserId(currentUser.userId());
        audit.setAction("STUDENT_STATUS_REQUEST_" + status);
        audit.setDetails("requestId=" + request.getId() + ", indeksId=" + request.getStudentIndeks().getId());
        auditRepo.save(audit);
    }

    private StudentStatusRequest requirePendingRequest(Long id) {
        StudentStatusRequest request = requestRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Zahtev ne postoji: " + id));
        if (request.getStatus() != StudentStatusRequest.Status.PENDING) {
            throw ApiException.conflict("STATUS_REQUEST_ALREADY_DECIDED", "Zahtev je vec obradjen.");
        }
        return request;
    }

    private void validateTransition(StudentStatus old, StudentStatus target, StudentStatusChangeRequest request) {
        boolean allowed =
                (old == StudentStatus.AKTIVAN && EnumSet.of(StudentStatus.MIROVANJE, StudentStatus.ISPISAN,
                        StudentStatus.DIPLOMIRAO, StudentStatus.NEAKTIVAN).contains(target))
                || (old == StudentStatus.MIROVANJE && target == StudentStatus.AKTIVAN)
                || (old == StudentStatus.NEAKTIVAN && target == StudentStatus.AKTIVAN)
                || (old == StudentStatus.ISPISAN && target == StudentStatus.AKTIVAN);
        if (!allowed) {
            throw ApiException.conflict("INVALID_STUDENT_STATUS_TRANSITION",
                    "Nedozvoljena promena statusa: " + old + " -> " + target + ".");
        }
        if (target == StudentStatus.MIROVANJE && (request.getValidFrom() == null || request.getValidTo() == null
                || request.getValidTo().isBefore(request.getValidFrom()))) {
            throw ApiException.badRequest("Mirovanje zahteva validan period od-do.");
        }
    }

    private StudentStatus parseStatus(String value) {
        try {
            return StudentStatus.valueOf(value.trim().toUpperCase());
        } catch (RuntimeException ex) {
            throw ApiException.badRequest("Nepoznat status studenta.");
        }
    }

    private StudentStatus effectiveStatus(StudentIndeks indeks) {
        return indeks.getStatus() == null ? (indeks.isAktivan() ? StudentStatus.AKTIVAN : StudentStatus.NEAKTIVAN) : indeks.getStatus();
    }

    private StudentIndeks requireIndeks(Long id) {
        return indeksRepo.findById(id).orElseThrow(() -> ApiException.notFound("StudentIndeks ne postoji: " + id));
    }

    private StudentStatusDTO toStatus(StudentIndeks indeks) {
        return new StudentStatusDTO(indeks.getId(), effectiveStatus(indeks).name(), indeks.getStatusReason(),
                indeks.getActivatedAt(), indeks.getDeactivatedAt());
    }

    private StudentStatusHistoryDTO toHistory(StudentStatusHistory item) {
        return new StudentStatusHistoryDTO(item.getId(), item.getStudentIndeks().getId(),
                item.getOldStatus() == null ? null : item.getOldStatus().name(), item.getNewStatus().name(),
                item.getReason(), item.getValidFrom(), item.getValidTo(), item.getChangedByUserId(), item.getCreatedAt());
    }

    private StudentStatusRequestDTO toRequest(StudentStatusRequest item) {
        return new StudentStatusRequestDTO(item.getId(), item.getStudentIndeks().getId(), item.getType().name(),
                item.getStatus().name(), item.getReason(), item.getRequestedFrom(), item.getRequestedTo(),
                item.getSubmittedByUserId(), item.getDecidedByUserId(), item.getDecisionNote(), item.getCreatedAt(), item.getDecidedAt());
    }
}
