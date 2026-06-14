package org.raflab.studsluzba.services;

import lombok.Getter;
import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class StudyYearEnrollmentPolicy {

    @Getter
    @Value("${enrollment.regular.minimum-ects-per-year:48}")
    private int regularMinimumEctsPerYear;

    @Getter
    @Value("${enrollment.conditional.minimum-ects-per-year:37}")
    private int conditionalMinimumEctsPerYear;

    @Getter
    @Value("${enrollment.transfer.max-ects:60}")
    private int maximumTransferredEcts;

    public int regularThresholdForCurrentYear(int currentStudyYear) {
        return Math.max(0, currentStudyYear) * regularMinimumEctsPerYear;
    }

    public int conditionalThresholdForCurrentYear(int currentStudyYear) {
        return Math.max(0, currentStudyYear) * conditionalMinimumEctsPerYear;
    }

    public StudyYearEnrollmentRequest.Type suggest(int currentStudyYear, int programDuration, int earnedEcts,
                                                   int regularThreshold, int conditionalThreshold) {
        if (currentStudyYear < programDuration && earnedEcts >= regularThreshold) {
            return StudyYearEnrollmentRequest.Type.ENROLL_NEXT_YEAR;
        }
        if (currentStudyYear < programDuration && earnedEcts >= conditionalThreshold) {
            return StudyYearEnrollmentRequest.Type.CONDITIONAL_ENROLLMENT;
        }
        return StudyYearEnrollmentRequest.Type.RENEW_YEAR;
    }

    public int requestedStudyYear(StudyYearEnrollmentRequest.Type type, int currentStudyYear) {
        return type == StudyYearEnrollmentRequest.Type.RENEW_YEAR ? currentStudyYear : currentStudyYear + 1;
    }
}
