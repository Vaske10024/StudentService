package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.documents.GeneratedCertificate;
import org.raflab.studsluzba.model.documents.StudentRequest;
import org.raflab.studsluzba.repositories.documents.GeneratedCertificateRepository;
import org.raflab.studsluzba.repositories.documents.StudentDocumentRepository;
import org.raflab.studsluzba.model.documents.StudentDocument;
import org.raflab.studsluzba.model.documents.DocumentType;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateGenerationService {
    private final FileStorageService storage;
    private final GeneratedCertificateRepository repo;
    private final StudentDocumentRepository documentRepo;

    public GeneratedCertificate generate(StudentRequest request) {
        String code = UUID.randomUUID().toString();
        String text = "%PDF-1.4\n% Student service certificate\n1 0 obj<</Type/Catalog>>endobj\n"
                + "% requestId=" + request.getId() + " verification=" + code + "\n%%EOF";
        GeneratedCertificate certificate = new GeneratedCertificate();
        certificate.setStudentRequest(request);
        certificate.setVerificationCode(code);
        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);
        certificate.setStorageKey(storage.store(bytes, "application/pdf"));
        GeneratedCertificate saved = repo.save(certificate);
        StudentDocument document = new StudentDocument();
        document.setStudentRequest(request);
        document.setStudentIndeks(request.getStudentIndeks());
        document.setType(DocumentType.CERTIFICATE);
        document.setOriginalName("certificate-" + request.getId() + ".pdf");
        document.setContentType("application/pdf");
        document.setSizeBytes(bytes.length);
        document.setStorageKey(saved.getStorageKey());
        documentRepo.save(document);
        return saved;
    }
}
