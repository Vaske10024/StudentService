package org.raflab.studsluzba.controllers;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.documents.*;
import org.raflab.studsluzba.model.dtos.StudentRequestCreateDTO;
import org.raflab.studsluzba.model.dtos.StudentDocumentDTO;
import org.raflab.studsluzba.model.dtos.StudentRequestDTO;
import org.raflab.studsluzba.services.DocumentRequestService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
public class DocumentRequestController {
    private final DocumentRequestService service;
    @PostMapping public StudentRequestDTO create(@RequestBody @Valid StudentRequestCreateDTO dto) { return service.create(dto); }
    @GetMapping public List<StudentRequestDTO> list(@RequestParam Long indeksId) { return service.list(indeksId); }
    @GetMapping("/admin") public List<StudentRequestDTO> listAll() { return service.listAll(); }
    @GetMapping("/documents") public List<StudentDocumentDTO> documents(@RequestParam Long indeksId) { return service.documents(indeksId); }
    @PostMapping("/{id}/approve") public StudentRequestDTO approve(@PathVariable Long id, @RequestParam(required=false) String note) { return service.decide(id, true, note); }
    @PostMapping("/{id}/reject") public StudentRequestDTO reject(@PathVariable Long id, @RequestParam(required=false) String note) { return service.decide(id, false, note); }
    @PostMapping("/{id}/documents")
    public StudentDocumentDTO upload(@PathVariable Long id, @RequestParam DocumentType type, @RequestParam MultipartFile file) throws IOException {
        return service.upload(id, type, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }
    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        StudentDocumentDTO document = service.document(id);
        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (document.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(document.getContentType());
            } catch (InvalidMediaTypeException ignored) {
                mediaType = MediaType.APPLICATION_OCTET_STREAM;
            }
        }
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + safeFileName(document.getOriginalName()) + "\"")
                .body(service.download(id));
    }

    private String safeFileName(String name) {
        return name == null ? "document.pdf" : name.replace("\"", "");
    }
}
