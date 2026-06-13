package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.documents.GeneratedCertificate;
import org.raflab.studsluzba.model.documents.StudentRequest;
import org.raflab.studsluzba.repositories.documents.GeneratedCertificateRepository;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateGenerationService {
    private final FileStorageService storage;
    private final GeneratedCertificateRepository repo;

    public GeneratedCertificate generate(StudentRequest request) {
        String code = UUID.randomUUID().toString();
        String text = "%PDF-1.4\n% Student service certificate\n1 0 obj<</Type/Catalog>>endobj\n"
                + "% requestId=" + request.getId() + " verification=" + code + "\n%%EOF";
        GeneratedCertificate certificate = new GeneratedCertificate();
        certificate.setStudentRequest(request);
        certificate.setVerificationCode(code);
        certificate.setStorageKey(storage.store(text.getBytes(StandardCharsets.US_ASCII), "application/pdf"));
        return repo.save(certificate);
    }
}
