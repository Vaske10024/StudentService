package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequest;
import org.raflab.studsluzba.services.StudyYearEnrollmentService;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/enrollment/year-requests")
@RequiredArgsConstructor
public class StudyYearEnrollmentController {

    private final StudyYearEnrollmentService service;

    @GetMapping("/me/eligibility")
    public StudyYearEnrollmentEligibilityDTO eligibility() {
        return service.myEligibility();
    }

    @GetMapping("/me")
    public List<StudyYearEnrollmentRequestDTO> myRequests() {
        return service.myRequests();
    }

    @PostMapping("/me")
    public StudyYearEnrollmentRequestDTO submit(@RequestBody @Valid StudyYearEnrollmentRequestCreateDTO dto) {
        return service.submit(dto);
    }

    @PutMapping("/me/{id}")
    public StudyYearEnrollmentRequestDTO updateAndResubmit(
            @PathVariable Long id, @RequestBody @Valid StudyYearEnrollmentRequestUpdateDTO dto) {
        return service.updateAndResubmit(id, dto);
    }

    @PostMapping("/me/{id}/cancel")
    public StudyYearEnrollmentRequestDTO cancel(@PathVariable Long id) {
        return service.cancel(id);
    }

    @GetMapping("/admin")
    public List<StudyYearEnrollmentRequestDTO> adminList(
            @RequestParam(required = false) StudyYearEnrollmentRequest.Status status,
            @RequestParam(required = false) StudyYearEnrollmentRequest.Type type,
            @RequestParam(required = false) Long targetSchoolYearId,
            @RequestParam(required = false) Long studentIndeksId) {
        return service.adminList(status, type, targetSchoolYearId, studentIndeksId);
    }

    @GetMapping("/admin/{id}")
    public StudyYearEnrollmentRequestDTO adminDetail(@PathVariable Long id) {
        return service.adminDetail(id);
    }

    @PatchMapping("/admin/{id}/checklist")
    public StudyYearEnrollmentRequestDTO checklist(
            @PathVariable Long id, @RequestBody @Valid StudyYearEnrollmentChecklistDTO dto) {
        return service.updateChecklist(id, dto);
    }

    @PostMapping("/admin/{id}/approve")
    public StudyYearEnrollmentRequestDTO approve(@PathVariable Long id) {
        return service.approve(id);
    }

    @PostMapping("/admin/{id}/reject")
    public StudyYearEnrollmentRequestDTO reject(
            @PathVariable Long id, @RequestBody @Valid StudyYearEnrollmentDecisionDTO dto) {
        return service.reject(id, dto.getReason());
    }

    @PostMapping("/admin/{id}/needs-changes")
    public StudyYearEnrollmentRequestDTO needsChanges(
            @PathVariable Long id, @RequestBody @Valid StudyYearEnrollmentDecisionDTO dto) {
        return service.needsChanges(id, dto.getReason());
    }
}
