package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class StudyYearEnrollmentRequestDTO {
    private Long id;
    private Long indeksId;
    private String studentName;
    private String indexLabel;
    private String type;
    private String status;
    private Integer currentStudyYear;
    private Integer requestedStudyYear;
    private Integer earnedEctsSnapshot;
    private SkolskaGodinaDTO currentSchoolYear;
    private SkolskaGodinaDTO targetSchoolYear;
    private boolean contractReceived;
    private boolean paymentConfirmed;
    private boolean documentationComplete;
    private String studentNote;
    private String adminNote;
    private Long submittedByUserId;
    private Long decidedByUserId;
    private Long approvedEnrollmentId;
    private Long approvedRenewalId;
    private LocalDateTime submittedAt;
    private LocalDateTime updatedAt;
    private LocalDateTime decidedAt;
    private List<PredmetDTO> transferredSubjects = new ArrayList<>();
    private List<StudyYearEnrollmentRequestHistoryDTO> history = new ArrayList<>();
}
