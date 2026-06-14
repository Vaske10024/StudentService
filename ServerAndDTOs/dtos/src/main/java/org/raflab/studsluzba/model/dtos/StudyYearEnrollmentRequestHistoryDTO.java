package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudyYearEnrollmentRequestHistoryDTO {
    private Long id;
    private String oldStatus;
    private String newStatus;
    private String note;
    private Long actorUserId;
    private LocalDateTime createdAt;
}
