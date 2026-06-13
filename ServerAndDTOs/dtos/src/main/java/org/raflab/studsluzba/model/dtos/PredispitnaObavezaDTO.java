package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PredispitnaObavezaDTO {
    private Long id;
    private String vrsta;
    private Integer maxPoeni;
    private Long predmetId;
    private String predmetNaziv;
    private Long skolskaGodinaId;
    private String skolskaGodina;
}
