package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class AcademicProgressService {
    private final IspitQueryRepository examRepo;
    public double averageGrade(Long indeksId) {
        Double average = examRepo.averagePassedGrade(indeksId);
        return average == null ? 0.0 : average;
    }
}
