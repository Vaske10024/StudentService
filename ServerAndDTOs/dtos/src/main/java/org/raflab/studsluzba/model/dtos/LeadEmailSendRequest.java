package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadEmailSendRequest {
    private Long templateId;

    @NotBlank
    @Size(max = 255)
    private String subject;

    @NotBlank
    @Size(max = 10000)
    private String body;
}
