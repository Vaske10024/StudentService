package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.StudentStatusRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudentStatusRequestRepository extends JpaRepository<StudentStatusRequest, Long> {
    List<StudentStatusRequest> findByStudentIndeksIdOrderByCreatedAtDesc(Long indeksId);
    List<StudentStatusRequest> findAllByOrderByCreatedAtDesc();
}
