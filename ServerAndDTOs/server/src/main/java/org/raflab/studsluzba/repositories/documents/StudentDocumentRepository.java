package org.raflab.studsluzba.repositories.documents;
import org.raflab.studsluzba.model.documents.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface StudentDocumentRepository extends JpaRepository<StudentDocument, Long> {
    List<StudentDocument> findByStudentIndeksIdOrderByCreatedAtDesc(Long indeksId);
}
