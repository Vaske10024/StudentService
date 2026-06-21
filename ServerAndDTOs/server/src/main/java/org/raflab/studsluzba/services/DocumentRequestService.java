package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.StudentRequestCreateDTO;
import org.raflab.studsluzba.model.dtos.StudentDocumentDTO;
import org.raflab.studsluzba.model.dtos.StudentRequestDTO;
import org.raflab.studsluzba.model.dtos.StudentStatusChangeRequest;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.documents.*;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.documents.*;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.model.security.Permission;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class DocumentRequestService {
    private final StudentRequestRepository requestRepo;
    private final StudentRequestStatusHistoryRepository historyRepo;
    private final StudentDocumentRepository documentRepo;
    private final StudentIndeksRepository indeksRepo;
    private final CurrentUser currentUser;
    private final StudentLifecycleService lifecycleService;
    private final CertificateGenerationService certificateService;
    private final FileStorageService storage;
    private final PermissionService permissions;
    private final NotificationService notificationService;

    public StudentRequestDTO create(StudentRequestCreateDTO dto) {
        currentUser.requireAdminOrStudentOwnsIndeks(dto.getIndeksId());
        StudentRequest request = new StudentRequest();
        request.setStudentIndeks(indeksRepo.findById(dto.getIndeksId()).orElseThrow(() -> ApiException.notFound("Indeks ne postoji.")));
        try { request.setType(RequestType.valueOf(dto.getType().trim().toUpperCase())); }
        catch (RuntimeException e) { throw ApiException.badRequest("Nepoznat tip zahteva."); }
        request.setReason(dto.getReason()); request.setRequestedFrom(dto.getRequestedFrom()); request.setRequestedTo(dto.getRequestedTo());
        request.setSubmittedByUserId(currentUser.userId());
        return toDto(requestRepo.save(request));
    }

    public StudentRequestDTO decide(Long id, boolean approved, String note) {
        permissions.require(Permission.DOCUMENT_DECIDE);
        StudentRequest request = require(id);
        if (request.getStatus() != StudentRequest.Status.SUBMITTED && request.getStatus() != StudentRequest.Status.IN_REVIEW) {
            throw ApiException.conflict("REQUEST_ALREADY_DECIDED", "Zahtev je vec obradjen.");
        }
        StudentRequest.Status old = request.getStatus();
        request.setStatus(approved ? StudentRequest.Status.APPROVED : StudentRequest.Status.REJECTED);
        request.setDecisionNote(note); request.setDecidedAt(LocalDateTime.now()); request.setDecidedByUserId(currentUser.userId());
        if (approved) applyEffect(request);
        StudentRequest saved = requestRepo.save(request);
        history(saved, old, saved.getStatus(), note);
        notificationService.notifyStudent(saved.getStudentIndeks().getId(), "REQUEST_DECISION", "Zahtev je obradjen",
                "Zahtev " + saved.getType() + " ima status " + saved.getStatus() + ".");
        return toDto(saved);
    }

    public StudentDocumentDTO upload(Long requestId, DocumentType type, String name, String contentType, byte[] bytes) {
        StudentRequest request = require(requestId);
        currentUser.requireAdminOrStudentOwnsIndeks(request.getStudentIndeks().getId());
        StudentDocument doc = new StudentDocument();
        doc.setStudentRequest(request); doc.setStudentIndeks(request.getStudentIndeks()); doc.setType(type);
        doc.setOriginalName(name); doc.setContentType(contentType); doc.setSizeBytes(bytes.length);
        doc.setStorageKey(storage.store(bytes, contentType));
        return toDocumentDto(documentRepo.save(doc));
    }

    @Transactional(readOnly = true)
    public byte[] download(Long documentId) {
        StudentDocument doc = documentRepo.findById(documentId).orElseThrow(() -> ApiException.notFound("Dokument ne postoji."));
        currentUser.requireAdminOrStudentOwnsIndeks(doc.getStudentIndeks().getId());
        return storage.load(doc.getStorageKey());
    }

    @Transactional(readOnly = true)
    public StudentDocumentDTO document(Long documentId) {
        StudentDocument doc = documentRepo.findById(documentId).orElseThrow(() -> ApiException.notFound("Dokument ne postoji."));
        currentUser.requireAdminOrStudentOwnsIndeks(doc.getStudentIndeks().getId());
        return toDocumentDto(doc);
    }

    @Transactional(readOnly = true)
    public List<StudentRequestDTO> list(Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return requestRepo.findByStudentIndeksIdOrderByCreatedAtDesc(indeksId).stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentRequestDTO> listAll() {
        permissions.require(Permission.DOCUMENT_DECIDE);
        return requestRepo.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentDTO> documents(Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return documentRepo.findByStudentIndeksIdOrderByCreatedAtDesc(indeksId).stream()
                .map(this::toDocumentDto)
                .collect(Collectors.toList());
    }

    private StudentRequest require(Long id) {
        return requestRepo.findById(id).orElseThrow(() -> ApiException.notFound("Zahtev ne postoji: " + id));
    }

    private void applyEffect(StudentRequest request) {
        if (request.getType() == RequestType.MIROVANJE || request.getType() == RequestType.ISPIS) {
            StudentStatusChangeRequest change = new StudentStatusChangeRequest();
            change.setNewStatus(request.getType() == RequestType.MIROVANJE ? "MIROVANJE" : "ISPISAN");
            change.setReason(request.getReason()); change.setValidFrom(request.getRequestedFrom()); change.setValidTo(request.getRequestedTo());
            lifecycleService.changeStatus(request.getStudentIndeks().getId(), change);
        }
        if (request.getType() == RequestType.POTVRDA_O_STUDIRANJU || request.getType() == RequestType.UVERENJE_O_POLOZENIM_ISPITIMA) {
            certificateService.generate(request);
        }
    }

    private void history(StudentRequest request, StudentRequest.Status oldStatus, StudentRequest.Status newStatus, String note) {
        StudentRequestStatusHistory history = new StudentRequestStatusHistory();
        history.setStudentRequest(request); history.setOldStatus(oldStatus.name()); history.setNewStatus(newStatus.name());
        history.setNote(note); history.setActorUserId(currentUser.userId()); historyRepo.save(history);
    }

    private StudentRequestDTO toDto(StudentRequest request) {
        StudentRequestDTO dto = new StudentRequestDTO();
        dto.setId(request.getId());
        StudentIndeks indeks = request.getStudentIndeks();
        if (indeks != null) {
            dto.setIndeksId(indeks.getId());
            dto.setIndexLabel(indeks.getStudProgramOznaka() + " " + indeks.getBroj() + "/" + indeks.getGodina());
            if (indeks.getStudent() != null) {
                dto.setStudentName(indeks.getStudent().getIme() + " " + indeks.getStudent().getPrezime());
            }
        }
        dto.setType(request.getType() == null ? null : request.getType().name());
        dto.setStatus(request.getStatus() == null ? null : request.getStatus().name());
        dto.setReason(request.getReason());
        dto.setRequestedFrom(request.getRequestedFrom());
        dto.setRequestedTo(request.getRequestedTo());
        dto.setSubmittedByUserId(request.getSubmittedByUserId());
        dto.setDecidedByUserId(request.getDecidedByUserId());
        dto.setDecisionNote(request.getDecisionNote());
        dto.setCreatedAt(request.getCreatedAt());
        dto.setDecidedAt(request.getDecidedAt());
        return dto;
    }

    private StudentDocumentDTO toDocumentDto(StudentDocument document) {
        return new StudentDocumentDTO(
                document.getId(),
                document.getStudentIndeks() == null ? null : document.getStudentIndeks().getId(),
                document.getStudentRequest() == null ? null : document.getStudentRequest().getId(),
                document.getType() == null ? null : document.getType().name(),
                document.getOriginalName(),
                document.getContentType(),
                document.getSizeBytes(),
                document.getCreatedAt()
        );
    }
}
