package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatusRequestDTO {
    private Long id;
    private Long indeksId;
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
