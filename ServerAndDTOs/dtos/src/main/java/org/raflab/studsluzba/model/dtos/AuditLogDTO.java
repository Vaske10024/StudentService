package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuditLogDTO {
    private Long id;
    private Long actorUserId;
    private String action;
    private String details;
    private LocalDateTime createdAt;
}
