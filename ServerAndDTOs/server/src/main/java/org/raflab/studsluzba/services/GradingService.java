package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GradingService {
    private final AuditLogRepository auditLogRepository;
    private final CurrentUser currentUser;

    @Value("${grading.exam.max-points:70}")
    private int examMaxPoints;

    @Value("${grading.predispit.max-total-points:100}")
    private int predispitMaxTotalPoints;

    public int getExamMaxPoints() {
        return examMaxPoints;
    }

    public int getPredispitMaxTotalPoints() {
        return predispitMaxTotalPoints;
    }

    public void validateExamPoints(Integer poeni) {
        if (poeni == null || poeni < 0 || poeni > examMaxPoints) {
            throw new IllegalArgumentException("Ispitni poeni moraju biti u opsegu [0.." + examMaxPoints + "].");
        }
    }

    public void validateTotalPoints(int ukupno) {
        if (ukupno < 0 || ukupno > 100) {
            throw new IllegalStateException("Ukupni poeni moraju biti u opsegu [0..100].");
        }
    }

    public int gradeForTotalPoints(int ukupno) {
        validateTotalPoints(ukupno);
        if (ukupno < 51) return 5;
        if (ukupno <= 60) return 6;
        if (ukupno <= 70) return 7;
        if (ukupno <= 80) return 8;
        if (ukupno <= 90) return 9;
        return 10;
    }

    public void auditManualOverride(Long prijavaId, Integer overrideOcena, Integer poeni) {
        AuditLog log = new AuditLog();
        try {
            log.setActorUserId(currentUser.userId());
        } catch (Exception ignored) {
            log.setActorUserId(null);
        }
        log.setAction("GRADE_MANUAL_OVERRIDE");
        log.setDetails("prijavaId=" + prijavaId + ", overrideOcena=" + overrideOcena + ", poeni=" + poeni);
        auditLogRepository.save(log);
    }
}
