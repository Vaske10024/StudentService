package org.raflab.studsluzba.services;
import org.junit.jupiter.api.Test;import org.raflab.studsluzba.repositories.*;import org.raflab.studsluzba.repositories.schedule.*;import org.raflab.studsluzba.security.ApiException;import java.time.LocalDateTime;import java.util.List;import static org.assertj.core.api.Assertions.assertThatThrownBy;import static org.mockito.Mockito.*;
class ScheduleServiceTest{
 @Test void overlappingSessionIsRejected(){
  ClassSessionRepository sessions=mock(ClassSessionRepository.class);
  ScheduleService service=new ScheduleService(mock(RoomRepository.class),mock(StudentGroupRepository.class),sessions,mock(ExamRoomAssignmentRepository.class),mock(NastavnikRepository.class),mock(IspitRepository.class));
  LocalDateTime start=LocalDateTime.now(),end=start.plusHours(1);when(sessions.conflicts(start,end,1L,2L,3L)).thenReturn(List.of(mock(org.raflab.studsluzba.model.schedule.ClassSession.class)));
  assertThatThrownBy(()->service.createSession("Termin",1L,3L,2L,start,end)).isInstanceOf(ApiException.class).extracting("code").isEqualTo("SCHEDULE_CONFLICT");
 }
}
