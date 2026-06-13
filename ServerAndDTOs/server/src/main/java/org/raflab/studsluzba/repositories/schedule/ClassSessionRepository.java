package org.raflab.studsluzba.repositories.schedule;
import org.raflab.studsluzba.model.schedule.ClassSession;import org.springframework.data.jpa.repository.*;import org.springframework.data.repository.query.Param;import java.time.LocalDateTime;import java.util.List;
public interface ClassSessionRepository extends JpaRepository<ClassSession,Long>{
 @Query("select s from ClassSession s where s.startsAt < :end and s.endsAt > :start and (s.room.id=:room or s.professor.id=:professor or s.studentGroup.id=:group)")
 List<ClassSession> conflicts(@Param("start")LocalDateTime start,@Param("end")LocalDateTime end,@Param("room")Long room,@Param("professor")Long professor,@Param("group")Long group);
 List<ClassSession> findByStudentGroupIdOrderByStartsAt(Long groupId);
}
