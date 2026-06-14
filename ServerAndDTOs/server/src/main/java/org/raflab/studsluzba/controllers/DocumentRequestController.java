package org.raflab.studsluzba.controllers;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.documents.*;
import org.raflab.studsluzba.model.dtos.StudentRequestCreateDTO;
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
    @PostMapping public StudentRequest create(@RequestBody @Valid StudentRequestCreateDTO dto) { return service.create(dto); }
    @GetMapping public List<StudentRequest> list(@RequestParam Long indeksId) { return service.list(indeksId); }
    @GetMapping("/admin") public List<StudentRequest> listAll() { return service.listAll(); }
    @PostMapping("/{id}/approve") public StudentRequest approve(@PathVariable Long id, @RequestParam(required=false) String note) { return service.decide(id, true, note); }
    @PostMapping("/{id}/reject") public StudentRequest reject(@PathVariable Long id, @RequestParam(required=false) String note) { return service.decide(id, false, note); }
    @PostMapping("/{id}/documents")
    public StudentDocument upload(@PathVariable Long id, @RequestParam DocumentType type, @RequestParam MultipartFile file) throws IOException {
        return service.upload(id, type, file.getOriginalFilename(), file.getContentType(), file.getBytes());
    }
    @GetMapping("/documents/{id}")
    public ResponseEntity<byte[]> download(@PathVariable Long id) {
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_OCTET_STREAM).body(service.download(id));
    }
}
