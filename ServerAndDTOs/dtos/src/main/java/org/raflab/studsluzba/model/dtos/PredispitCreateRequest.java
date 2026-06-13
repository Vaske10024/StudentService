package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;



@Data
public class PredispitCreateRequest {
    @NotNull
    private Long predmetId;
    @NotNull private Long skolskaGodinaId;
    @NotBlank
    private String vrsta;
    @Min(0) private int maxPoeni;
}
