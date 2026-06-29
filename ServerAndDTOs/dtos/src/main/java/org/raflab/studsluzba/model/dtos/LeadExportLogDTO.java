package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LeadExportLogDTO {
    private Long id;
    private Long exportedByUserId;
    private String exportedByUsername;
    private String exporterRole;
    private String exportType;
    private boolean masked;
    private int recordCount;
    private String filters;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
