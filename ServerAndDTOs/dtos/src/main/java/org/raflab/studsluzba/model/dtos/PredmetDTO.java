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
}
