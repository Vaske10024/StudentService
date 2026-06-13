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
    public EnrollmentApplication submit(@RequestHeader("Idempotency-Key") String key, @RequestBody @Valid EnrollmentApplicationCreateDTO dto) {
        return service.submit(key, dto);
    }
    @PostMapping("/applications/{id}/approve")
    public EnrollmentApplication approve(@PathVariable Long id, @RequestBody @Valid EnrollmentApprovalDTO dto) {
        return service.approve(id, dto.getInitialPassword());
    }
    @PostMapping("/applications/{id}/reject")
    public EnrollmentApplication reject(@PathVariable Long id, @RequestParam String reason) { return service.reject(id, reason); }
    @GetMapping("/applications/{id}")
    public EnrollmentApplication get(@PathVariable Long id) { return service.require(id); }
    @GetMapping("/applications")
    public List<EnrollmentApplication> list() { return service.list(); }
}
