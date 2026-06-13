package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.schedule.*;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.schedule.*;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;import java.time.LocalDateTime;import java.util.List;
@Service @RequiredArgsConstructor
public class ScheduleService {
 private final RoomRepository roomRepo;private final StudentGroupRepository groupRepo;private final ClassSessionRepository sessionRepo;
 private final ExamRoomAssignmentRepository assignmentRepo;private final NastavnikRepository professorRepo;private final IspitRepository examRepo;
 public ClassSession createSession(String title,Long roomId,Long groupId,Long professorId,LocalDateTime start,LocalDateTime end){
  if(start==null||end==null||!end.isAfter(start))throw ApiException.badRequest("Kraj termina mora biti posle pocetka.");
  if(!sessionRepo.conflicts(start,end,roomId,professorId,groupId).isEmpty())throw ApiException.conflict("SCHEDULE_CONFLICT","Termin se preklapa za salu, profesora ili grupu.");
  ClassSession s=new ClassSession();s.setTitle(title);s.setStartsAt(start);s.setEndsAt(end);
  s.setRoom(roomRepo.findById(roomId).orElseThrow(()->ApiException.notFound("Sala ne postoji.")));
  s.setStudentGroup(groupRepo.findById(groupId).orElseThrow(()->ApiException.notFound("Grupa ne postoji.")));
  s.setProfessor(professorRepo.findById(professorId).orElseThrow(()->ApiException.notFound("Profesor ne postoji.")));return sessionRepo.save(s);
 }
 public ExamRoomAssignment assignExam(Long examId,Long roomId,int expected){
  Room room=roomRepo.findById(roomId).orElseThrow(()->ApiException.notFound("Sala ne postoji."));
  if(expected>room.getCapacity())throw ApiException.conflict("ROOM_CAPACITY_EXCEEDED","Kapacitet sale nije dovoljan za ispit.");
  ExamRoomAssignment a=new ExamRoomAssignment();a.setIspit(examRepo.findById(examId).orElseThrow(()->ApiException.notFound("Ispit ne postoji.")));a.setRoom(room);a.setExpectedStudents(expected);return assignmentRepo.save(a);
 }
 public Room createRoom(String code,int capacity,String location){if(capacity<1)throw ApiException.badRequest("Kapacitet mora biti pozitivan.");Room r=new Room();r.setCode(code);r.setCapacity(capacity);r.setLocation(location);return roomRepo.save(r);}
 public StudentGroup createGroup(String code,String name){StudentGroup g=new StudentGroup();g.setCode(code);g.setName(name);return groupRepo.save(g);}
 public List<ClassSession> groupCalendar(Long groupId){return sessionRepo.findByStudentGroupIdOrderByStartsAt(groupId);}
 public String groupCalendarIcs(Long groupId){StringBuilder s=new StringBuilder("BEGIN:VCALENDAR\r\nVERSION:2.0\r\n");for(ClassSession item:groupCalendar(groupId)){s.append("BEGIN:VEVENT\r\nUID:session-").append(item.getId()).append("\r\nDTSTART:").append(item.getStartsAt().toString().replace("-","").replace(":","")).append("\r\nDTEND:").append(item.getEndsAt().toString().replace("-","").replace(":","")).append("\r\nSUMMARY:").append(item.getTitle()).append("\r\nEND:VEVENT\r\n");}return s.append("END:VCALENDAR\r\n").toString();}
}
