package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class SkolskaGodinaRequest {
    @NotBlank
    private String oznaka;  // npr. "25/26"
    private Boolean aktivna = false;
}
