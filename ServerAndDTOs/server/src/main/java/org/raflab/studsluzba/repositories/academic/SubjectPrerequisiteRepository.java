package org.raflab.studsluzba.repositories.academic;
import org.raflab.studsluzba.model.academic.SubjectPrerequisite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface SubjectPrerequisiteRepository extends JpaRepository<SubjectPrerequisite,Long> {
    List<SubjectPrerequisite> findBySubjectId(Long subjectId);
}
