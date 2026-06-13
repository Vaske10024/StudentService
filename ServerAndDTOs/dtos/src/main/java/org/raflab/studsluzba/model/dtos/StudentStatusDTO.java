package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StudentStatusDTO {
    private Long indeksId;
    private String status;
    private String reason;
    private LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;
}
