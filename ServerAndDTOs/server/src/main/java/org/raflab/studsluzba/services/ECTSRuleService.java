package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.repositories.academic.ECTSRuleRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class ECTSRuleService {
    private final ECTSRuleRepository repo;
    public void assertEnrollmentAllowed(StudentIndeks indeks, int targetYear, boolean override, String overrideReason) {
        if (targetYear <= 1 || indeks.getStudijskiProgram() == null) return;
        repo.findByProgramIdAndTargetYear(indeks.getStudijskiProgram().getId(), targetYear).ifPresent(rule -> {
            int ects = indeks.getOstvarenoEspb() == null ? 0 : indeks.getOstvarenoEspb();
            if (ects < rule.getMinimumEcts() && (!override || overrideReason == null || overrideReason.trim().isEmpty())) {
                throw ApiException.conflict("ECTS_REQUIREMENT_NOT_MET", "Student nema dovoljan broj ESPB za upis naredne godine.");
            }
        });
    }
}
