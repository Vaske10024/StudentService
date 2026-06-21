package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ProgramTransferDTO {
    private Long id;
    private Long indeksId;
    private Long fromProgramId;
    private Long toProgramId;
    private String status;
    private String reason;
    private Long decidedByUserId;
    private LocalDateTime decidedAt;
    private LocalDateTime createdAt;
}
