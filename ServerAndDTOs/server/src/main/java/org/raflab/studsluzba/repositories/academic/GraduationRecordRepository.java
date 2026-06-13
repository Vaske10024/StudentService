package org.raflab.studsluzba.repositories.academic;
import org.raflab.studsluzba.model.academic.GraduationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
public interface GraduationRecordRepository extends JpaRepository<GraduationRecord,Long> {
    boolean existsByStudentIndeksId(Long indeksId);
}
