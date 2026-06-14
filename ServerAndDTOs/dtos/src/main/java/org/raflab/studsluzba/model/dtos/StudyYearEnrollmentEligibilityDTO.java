package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class StudyYearEnrollmentEligibilityDTO {
    private Long indeksId;
    private Integer currentStudyYear;
    private Integer requestedStudyYear;
    private Integer programDuration;
    private Integer earnedEcts;
    private Integer regularEnrollmentThreshold;
    private Integer conditionalEnrollmentThreshold;
    private String suggestedType;
    private boolean canSubmit;
    private String message;
    private SkolskaGodinaDTO currentSchoolYear;
    private SkolskaGodinaDTO targetSchoolYear;
    private List<PolozenPredmetDTO> passedSubjects = new ArrayList<>();
    private List<PredmetDTO> transferableSubjects = new ArrayList<>();
}
