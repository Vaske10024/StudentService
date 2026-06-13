package org.raflab.studsluzba.repositories.documents;
import org.raflab.studsluzba.model.documents.StudentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudentRequestRepository extends JpaRepository<StudentRequest, Long> {
    List<StudentRequest> findByStudentIndeksIdOrderByCreatedAtDesc(Long indeksId);
}
