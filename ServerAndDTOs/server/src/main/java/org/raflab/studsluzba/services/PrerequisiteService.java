package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.academic.SubjectPrerequisite;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.academic.SubjectPrerequisiteRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class PrerequisiteService {
    private final SubjectPrerequisiteRepository prerequisiteRepo;
    private final IspitQueryRepository examRepo;
    public void assertSatisfied(Long indeksId, Long subjectId) {
        for (SubjectPrerequisite item : prerequisiteRepo.findBySubjectId(subjectId)) {
            if (!examRepo.existsPassedSubject(indeksId, item.getPrerequisite().getId())) {
                throw ApiException.conflict("PREREQUISITE_NOT_SATISFIED",
                        "Student nije polozio preduslov: " + item.getPrerequisite().getNaziv());
            }
        }
    }
}
