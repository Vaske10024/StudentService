package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class GraduationRecordDTO {
    private Long id;
    private Long indeksId;
    private Integer earnedEcts;
    private Double averageGrade;
    private String note;
    private Long approvedByUserId;
    private LocalDateTime graduatedAt;
}
