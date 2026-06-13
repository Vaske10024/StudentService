package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentIndeksService {

    private final StudentIndeksRepository studentIndeksRepository;

    @Transactional(readOnly = true)
    public int findBroj(int godina, String studProgramOznaka) {
        List<Integer> brojeviList = studentIndeksRepository
                .findBrojeviByGodinaAndStudProgramOznaka(godina, studProgramOznaka);
        return findNextAvailableNumber(brojeviList);
    }

    @Transactional
    public int allocateNextBrojForUpdate(int godina, String studProgramOznaka) {
        List<Integer> brojeviList = studentIndeksRepository
                .lockIndeksiForNumberAllocation(godina, studProgramOznaka)
                .stream()
                .map(StudentIndeks::getBroj)
                .collect(Collectors.toList());
        return findNextAvailableNumber(brojeviList);
    }

    int findNextAvailableNumber(List<Integer> brojeviList) {
        if (brojeviList == null || brojeviList.isEmpty()) {
            return 1;
        }

        List<Integer> sorted = brojeviList.stream()
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        int expected = 1;
        for (int num : sorted) {
            if (num != expected) {
                return expected;
            }
            expected++;
        }
        return expected;
    }

    @Transactional(readOnly = true)
    public StudentIndeks findByStudentIdAndAktivan(Long studentPodaciId) {
        return studentIndeksRepository.findAktivanStudentIndeksiByStudentPodaciId(studentPodaciId);
    }
}
