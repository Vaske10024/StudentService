package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class AvailableExamDTO {
    private Long examId;
    private Long periodId;
    private Long subjectId;
    private String subjectCode;
    private String subjectName;
    private String professorName;
    private LocalDate examDate;
    private LocalTime examTime;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime cancellationEnd;
    private boolean periodActive;
    private boolean locked;
    private boolean eligible;
    private String eligibilityCode;
    private String eligibilityMessage;
    private Long activeRegistrationId;
    private boolean cancellationAllowed;
}
