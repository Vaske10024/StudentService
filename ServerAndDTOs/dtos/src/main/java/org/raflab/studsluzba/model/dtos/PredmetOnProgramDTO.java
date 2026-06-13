package org.raflab.studsluzba.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredmetOnProgramDTO {
    private Long predmetId;
    private String sifra;
    private String naziv;
    private String opis;
    private Integer espb;

    private Long programId;

    // Ukupni semestar (1..8) – korisno za prikaz, kompatibilno sa starim
    private Integer semestar;

    // NOVO: godina studija (1..4) i semestar u godini (1..2)
    private Integer godinaStudija;
    private Integer semestarUGodini;

    private Integer fondPredavanja;
    private Integer fondVezbi;
    private Integer fondPraktikum;
}
