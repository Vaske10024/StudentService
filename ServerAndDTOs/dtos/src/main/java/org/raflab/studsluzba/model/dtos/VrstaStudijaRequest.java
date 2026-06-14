package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Data
public class VrstaStudijaRequest {
    @NotBlank
    @Size(max = 30)
    private String skracenica;

    @NotBlank
    @Size(max = 255)
    private String puniNaziv;
}
