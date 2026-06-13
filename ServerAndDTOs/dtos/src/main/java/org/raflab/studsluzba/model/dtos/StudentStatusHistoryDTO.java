package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatusHistoryDTO {
    private Long id;
    private Long indeksId;
    private String oldStatus;
    private String newStatus;
    private String reason;
    private LocalDate validFrom;
    private LocalDate validTo;
    private Long changedByUserId;
    private LocalDateTime createdAt;
}
