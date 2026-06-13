package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.*;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.StudentLifecycleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/student-lifecycle")
@RequiredArgsConstructor
@Validated
public class StudentLifecycleController {
    private final StudentLifecycleService service;
    private final CurrentUser currentUser;

    @GetMapping("/{indeksId}/status")
    public StudentStatusDTO status(@PathVariable Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return service.getStatus(indeksId);
    }

    @GetMapping("/{indeksId}/history")
    public List<StudentStatusHistoryDTO> history(@PathVariable Long indeksId) {
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        return service.history(indeksId);
    }

    @PostMapping("/{indeksId}/status")
    public StudentStatusDTO change(@PathVariable Long indeksId, @RequestBody @Valid StudentStatusChangeRequest request) {
        currentUser.requireAdmin();
        return service.changeStatus(indeksId, request);
    }

    @PostMapping("/requests")
    public StudentStatusRequestDTO createRequest(@RequestBody @Valid StudentStatusRequestCreateDTO request) {
        return service.createRequest(request);
    }

    @GetMapping("/{indeksId}/requests")
    public List<StudentStatusRequestDTO> requests(@PathVariable Long indeksId) {
        return service.requests(indeksId);
    }

    @PatchMapping("/requests/{id}/approve")
    public StudentStatusRequestDTO approve(@PathVariable Long id, @RequestParam(required = false) String note) {
        return service.approveRequest(id, note);
    }

    @PatchMapping("/requests/{id}/reject")
    public StudentStatusRequestDTO reject(@PathVariable Long id, @RequestParam(required = false) String note) {
        return service.rejectRequest(id, note);
    }
}
