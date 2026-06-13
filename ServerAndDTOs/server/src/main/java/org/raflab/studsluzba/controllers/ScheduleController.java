package org.raflab.studsluzba.controllers;
import lombok.RequiredArgsConstructor;import org.raflab.studsluzba.model.schedule.*;import org.raflab.studsluzba.security.CurrentUser;import org.raflab.studsluzba.services.ScheduleService;import org.springframework.web.bind.annotation.*;import java.time.LocalDateTime;import java.util.List;
@RestController @RequestMapping("/api/schedule") @RequiredArgsConstructor
public class ScheduleController{
 private final ScheduleService service;private final CurrentUser currentUser;
 @PostMapping("/rooms")public Room room(@RequestParam String code,@RequestParam int capacity,@RequestParam(required=false)String location){currentUser.requireAdmin();return service.createRoom(code,capacity,location);}
 @PostMapping("/groups")public StudentGroup group(@RequestParam String code,@RequestParam String name){currentUser.requireAdmin();return service.createGroup(code,name);}
 @PostMapping("/sessions")public ClassSession session(@RequestParam String title,@RequestParam Long roomId,@RequestParam Long groupId,@RequestParam Long professorId,@RequestParam LocalDateTime startsAt,@RequestParam LocalDateTime endsAt){currentUser.requireAdmin();return service.createSession(title,roomId,groupId,professorId,startsAt,endsAt);}
 @PostMapping("/exam-rooms")public ExamRoomAssignment examRoom(@RequestParam Long examId,@RequestParam Long roomId,@RequestParam int expectedStudents){currentUser.requireAdmin();return service.assignExam(examId,roomId,expectedStudents);}
 @GetMapping("/groups/{groupId}")public List<ClassSession> calendar(@PathVariable Long groupId){return service.groupCalendar(groupId);}
 @GetMapping(value="/groups/{groupId}.ics",produces="text/calendar")public String calendarIcs(@PathVariable Long groupId){return service.groupCalendarIcs(groupId);}
}
