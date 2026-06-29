package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LeadEmailTemplateRequest {
    @NotBlank
    @Size(max = 180)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String subject;

    @NotBlank
    @Size(max = 10000)
    private String body;

    @NotNull
    private Boolean active;
}
