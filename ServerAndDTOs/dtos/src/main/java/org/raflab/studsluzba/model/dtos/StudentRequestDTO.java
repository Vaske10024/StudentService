package org.raflab.studsluzba.model.dtos;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StudentRequestDTO {
    private Long id;
    private Long indeksId;
    private String studentName;
    private String indexLabel;
    private String type;
    private String status;
    private String reason;
    private LocalDate requestedFrom;
    private LocalDate requestedTo;
    private Long submittedByUserId;
    private Long decidedByUserId;
    private String decisionNote;
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
}
