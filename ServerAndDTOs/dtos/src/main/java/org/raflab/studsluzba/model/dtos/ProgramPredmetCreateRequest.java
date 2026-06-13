package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;

@Data
public class ProgramPredmetCreateRequest {
    @NotBlank
    private String sifra;

    @NotBlank
    private String naziv;

    private String opis;
    private Integer espb;

    // NOVO: godina (1..4) i semestar u toj godini (1..2)
    @Min(1) @Max(4)
    private Integer godinaStudija;

    @Min(1) @Max(2)
    private Integer semestarUGodini;

    // STARI INPUT (1..8) – ostavljen da ne pukne postojeći FE/pozivi
    @Deprecated
    @Min(1) @Max(8)
    private Integer semestar;

    private Integer fondPredavanja;
    private Integer fondVezbi;
    private Integer fondPraktikum;
}
