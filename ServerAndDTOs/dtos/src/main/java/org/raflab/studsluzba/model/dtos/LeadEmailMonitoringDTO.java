package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LeadEmailMonitoringDTO {
    private LeadEmailMessageDTO message;
    private LeadDTO lead;
}
