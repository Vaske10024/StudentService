package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PredmetDTO {
    private Long id;
    private String sifra;
    private String naziv;
    private String opis;
    private Integer espb;
    private String studProgramOznaka; // ako postoji
    private Integer predispitniPoeni;
    private Integer ispitniPoeni;
    private Integer ukupnoPoena;

    public PredmetDTO(Long id, String sifra, String naziv, String opis, Integer espb, String studProgramOznaka) {
        this.id = id;
        this.sifra = sifra;
        this.naziv = naziv;
        this.opis = opis;
        this.espb = espb;
        this.studProgramOznaka = studProgramOznaka;
    }
}
