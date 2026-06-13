package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RealizacijaPredmetaDTO {
    private Long id;
    private Long programPredmetId;
    private Long programId;
    private String programOznaka;
    private Long predmetId;
    private String predmetSifra;
    private String predmetNaziv;
    private Integer godinaStudija;
    private Integer semestarUGodini;
    private Long skolskaGodinaId;
    private String skolskaGodina;
    private String status;
}
