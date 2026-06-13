package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;

@Data
public class CancellationRequest {
    @NotBlank
    private String reason;
}
