package org.raflab.studsluzba.controllers;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.model.enrollment.EnrollmentApplication;
import org.raflab.studsluzba.services.EnrollmentWorkflowService;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/enrollment")
@RequiredArgsConstructor
public class EnrollmentController {
    private final EnrollmentWorkflowService service;

    @PostMapping("/applications")
    public EnrollmentApplicationDTO submit(@RequestHeader("Idempotency-Key") String key, @RequestBody @Valid EnrollmentApplicationCreateDTO dto) {
        return toDto(service.submit(key, dto));
    }

    @PostMapping("/applications/{id}/approve")
    public EnrollmentApplicationDTO approve(@PathVariable Long id, @RequestBody @Valid EnrollmentApprovalDTO dto) {
        return toDto(service.approve(id, dto.getInitialPassword()));
    }

    @PostMapping("/applications/{id}/reject")
    public EnrollmentApplicationDTO reject(@PathVariable Long id, @RequestParam String reason) {
        return toDto(service.reject(id, reason));
    }

    @GetMapping("/applications/{id}")
    public EnrollmentApplicationDTO get(@PathVariable Long id) {
        return toDto(service.require(id));
    }

    @GetMapping("/applications")
    public List<EnrollmentApplicationDTO> list() {
        return service.list().stream().map(this::toDto).collect(java.util.stream.Collectors.toList());
    }

    private EnrollmentApplicationDTO toDto(EnrollmentApplication application) {
        return new EnrollmentApplicationDTO(
                application.getId(),
                application.getStatus() == null ? null : application.getStatus().name(),
                application.getIme(),
                application.getPrezime(),
                application.getEmail(),
                application.getUsername(),
                application.getStudijskiProgramId(),
                application.getGodina(),
                application.getTuitionEur(),
                application.getCreatedStudent() == null ? null : application.getCreatedStudent().getId(),
                application.getCreatedIndeks() == null ? null : application.getCreatedIndeks().getId(),
                application.getDecisionReason(),
                application.getDecidedByUserId(),
                application.getDecidedAt(),
                application.getCreatedAt()
        );
    }
}
