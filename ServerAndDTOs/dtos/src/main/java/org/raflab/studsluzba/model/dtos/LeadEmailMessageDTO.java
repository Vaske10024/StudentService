package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LeadEmailMessageDTO {
    private Long id;
    private Long leadId;
    private Long templateId;
    private String templateName;
    private Long sentByUserId;
    private String sentByUsername;
    private String subject;
    private String body;
    private String status;
    private String providerMessageId;
    private String errorMessage;
    private LocalDateTime sentAt;
    private LocalDateTime createdAt;
}
