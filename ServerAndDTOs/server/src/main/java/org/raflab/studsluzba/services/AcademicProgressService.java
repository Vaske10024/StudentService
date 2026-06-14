package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Set;
@Service @RequiredArgsConstructor
public class AcademicProgressService {
    private final IspitQueryRepository examRepo;
    private final StudentIndeksRepository indeksRepo;
    public double averageGrade(Long indeksId) {
        Double average = examRepo.averagePassedGrade(indeksId);
        return average == null ? 0.0 : average;
    }

    @Transactional(readOnly = true)
    public int calculateEarnedEcts(Long indeksId) {
        Set<Long> countedSubjects = new HashSet<>();
        int ects = 0;
        for (PrijavaIspita attempt : examRepo.passedAttemptsForStudent(indeksId)) {
            Predmet subject = attempt.getPredmet();
            if (subject == null && attempt.getIspit() != null && attempt.getIspit().getDrziPredmet() != null) {
                subject = attempt.getIspit().getDrziPredmet().getPredmet();
            }
            if (subject != null && subject.getId() != null && countedSubjects.add(subject.getId())) {
                ects += subject.getEspb() == null ? 0 : subject.getEspb();
            }
        }
        return ects;
    }

    @Transactional
    public int recalculateEarnedEcts(Long indeksId) {
        StudentIndeks indeks = indeksRepo.findById(indeksId)
                .orElseThrow(() -> ApiException.notFound("Indeks ne postoji: " + indeksId));
        int ects = calculateEarnedEcts(indeksId);
        indeks.setOstvarenoEspb(ects);
        indeksRepo.save(indeks);
        return ects;
    }
}
