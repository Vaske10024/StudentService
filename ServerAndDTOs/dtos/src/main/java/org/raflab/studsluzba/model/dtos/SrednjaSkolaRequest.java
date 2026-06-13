package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;


@Data
public class SrednjaSkolaRequest {
    @NotBlank
    private String naziv;
    @NotBlank private String mesto;
    @NotBlank private String vrsta;
}
