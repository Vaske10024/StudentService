package org.raflab.studsluzba.services;

import org.raflab.studsluzba.security.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.nio.file.*;
import java.util.Set;
import java.util.UUID;

@Service
public class FileStorageService {
    private static final Set<String> ALLOWED = Set.of("application/pdf", "image/jpeg", "image/png");
    private final Path root;
    private final long maxBytes;

    public FileStorageService(@Value("${documents.storage.path:./data/documents}") String path,
                              @Value("${documents.max-size-bytes:5242880}") long maxBytes) {
        this.root = Paths.get(path).toAbsolutePath().normalize();
        this.maxBytes = maxBytes;
    }

    public String store(byte[] data, String contentType) {
        if (data == null || data.length == 0 || data.length > maxBytes) throw ApiException.badRequest("Nevalidna velicina dokumenta.");
        if (!ALLOWED.contains(contentType)) throw ApiException.badRequest("Nedozvoljen MIME tip dokumenta.");
        try {
            Files.createDirectories(root);
            String key = UUID.randomUUID().toString();
            Files.write(root.resolve(key), data, StandardOpenOption.CREATE_NEW);
            return key;
        } catch (IOException e) {
            throw ApiException.conflict("DOCUMENT_STORAGE_FAILED", "Cuvanje dokumenta nije uspelo.");
        }
    }

    public byte[] load(String key) {
        try {
            Path path = root.resolve(key).normalize();
            if (!path.startsWith(root)) throw ApiException.forbidden("Nevalidna putanja dokumenta.");
            return Files.readAllBytes(path);
        } catch (IOException e) {
            throw ApiException.notFound("Dokument nije pronadjen.");
        }
    }
}
