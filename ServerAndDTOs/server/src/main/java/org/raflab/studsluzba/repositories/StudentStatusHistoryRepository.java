package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.StudentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentStatusHistoryRepository extends JpaRepository<StudentStatusHistory, Long> {
    List<StudentStatusHistory> findByStudentIndeksIdOrderByCreatedAtDesc(Long indeksId);
}
