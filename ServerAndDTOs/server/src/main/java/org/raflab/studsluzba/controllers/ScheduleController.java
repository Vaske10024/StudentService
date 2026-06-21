package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.ClassSessionDTO;
import org.raflab.studsluzba.model.dtos.ExamRoomAssignmentDTO;
import org.raflab.studsluzba.model.dtos.RoomDTO;
import org.raflab.studsluzba.model.dtos.StudentGroupDTO;
import org.raflab.studsluzba.model.dtos.StudentGroupMembershipDTO;
import org.raflab.studsluzba.model.schedule.ClassSession;
import org.raflab.studsluzba.model.schedule.ExamRoomAssignment;
import org.raflab.studsluzba.model.schedule.Room;
import org.raflab.studsluzba.model.schedule.StudentGroup;
import org.raflab.studsluzba.model.schedule.StudentGroupMembership;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.ScheduleService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/schedule")
@RequiredArgsConstructor
public class ScheduleController {
    private final ScheduleService service;
    private final CurrentUser currentUser;

    @PostMapping("/rooms")
    public RoomDTO room(@RequestParam String code,
                        @RequestParam int capacity,
                        @RequestParam(required = false) String location) {
        currentUser.requireAdmin();
        return toDto(service.createRoom(code, capacity, location));
    }

    @PostMapping("/groups")
    public StudentGroupDTO group(@RequestParam String code, @RequestParam String name) {
        currentUser.requireAdmin();
        return toDto(service.createGroup(code, name));
    }

    @PostMapping("/groups/{groupId}/students/{indeksId}")
    public StudentGroupMembershipDTO addStudent(@PathVariable Long groupId, @PathVariable Long indeksId) {
        currentUser.requireAdmin();
        return toDto(service.addStudentToGroup(groupId, indeksId));
    }

    @PostMapping("/sessions")
    public ClassSessionDTO session(@RequestParam String title,
                                   @RequestParam Long roomId,
                                   @RequestParam Long groupId,
                                   @RequestParam Long professorId,
                                   @RequestParam LocalDateTime startsAt,
                                   @RequestParam LocalDateTime endsAt) {
        currentUser.requireAdmin();
        return toDto(service.createSession(title, roomId, groupId, professorId, startsAt, endsAt));
    }

    @PostMapping("/exam-rooms")
    public ExamRoomAssignmentDTO examRoom(@RequestParam Long examId,
                                          @RequestParam Long roomId,
                                          @RequestParam int expectedStudents) {
        currentUser.requireAdmin();
        return toDto(service.assignExam(examId, roomId, expectedStudents));
    }

    @GetMapping("/groups/{groupId}")
    public List<ClassSessionDTO> calendar(@PathVariable Long groupId) {
        currentUser.requireCanAccessScheduleGroup(groupId);
        return service.groupCalendar(groupId).stream().map(this::toDto).collect(Collectors.toList());
    }

    @GetMapping(value = "/groups/{groupId}.ics", produces = "text/calendar")
    public String calendarIcs(@PathVariable Long groupId) {
        currentUser.requireCanAccessScheduleGroup(groupId);
        return service.groupCalendarIcs(groupId);
    }

    private RoomDTO toDto(Room room) {
        return new RoomDTO(room.getId(), room.getCode(), room.getCapacity(), room.getLocation());
    }

    private StudentGroupDTO toDto(StudentGroup group) {
        return new StudentGroupDTO(group.getId(), group.getCode(), group.getName());
    }

    private StudentGroupMembershipDTO toDto(StudentGroupMembership membership) {
        return new StudentGroupMembershipDTO(
                membership.getId(),
                membership.getStudentGroup() == null ? null : membership.getStudentGroup().getId(),
                membership.getStudentGroup() == null ? null : membership.getStudentGroup().getCode(),
                membership.getStudentIndeks() == null ? null : membership.getStudentIndeks().getId()
        );
    }

    private ClassSessionDTO toDto(ClassSession session) {
        String professorName = null;
        if (session.getProfessor() != null) {
            professorName = session.getProfessor().getIme() + " " + session.getProfessor().getPrezime();
        }
        return new ClassSessionDTO(
                session.getId(),
                session.getTitle(),
                session.getRoom() == null ? null : session.getRoom().getId(),
                session.getRoom() == null ? null : session.getRoom().getCode(),
                session.getStudentGroup() == null ? null : session.getStudentGroup().getId(),
                session.getStudentGroup() == null ? null : session.getStudentGroup().getCode(),
                session.getProfessor() == null ? null : session.getProfessor().getId(),
                professorName,
                session.getStartsAt(),
                session.getEndsAt()
        );
    }

    private ExamRoomAssignmentDTO toDto(ExamRoomAssignment assignment) {
        return new ExamRoomAssignmentDTO(
                assignment.getId(),
                assignment.getIspit() == null ? null : assignment.getIspit().getId(),
                assignment.getRoom() == null ? null : assignment.getRoom().getId(),
                assignment.getRoom() == null ? null : assignment.getRoom().getCode(),
                assignment.getExpectedStudents()
        );
    }
}
