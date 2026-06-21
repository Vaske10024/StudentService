package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class EnrollmentApplicationDTO {
    private Long id;
    private String status;
    private String ime;
    private String prezime;
    private String email;
    private String username;
    private Long studijskiProgramId;
    private Integer godina;
    private BigDecimal tuitionEur;
    private Long createdStudentId;
    private Long createdIndeksId;
    private String decisionReason;
    private Long decidedByUserId;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
}
