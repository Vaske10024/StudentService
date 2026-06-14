package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class StudijskiProgramCreateRequest {
    @NotBlank
    private String oznaka;

    @NotBlank
    private String naziv;

    @NotNull
    private Integer godinaAkreditacije;

    @NotBlank
    private String zvanje;

    @NotNull
    @Min(1)
    @Max(8)
    private Integer trajanjeGodina;

    @NotNull
    @Min(1)
    private Integer ukupnoEspb;

    @NotNull
    private Long vrstaStudijaId;
}
