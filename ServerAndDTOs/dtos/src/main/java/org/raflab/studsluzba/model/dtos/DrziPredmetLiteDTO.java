package org.raflab.studsluzba.model.dtos;

import lombok.Data;

@Data
public class DrziPredmetLiteDTO {
    private Long id;
    private Long predmetId;
    private String predmetNaziv;
    private Long nastavnikId;
    private String nastavnikImePrezime;
    private Long realizacijaPredmetaId;
    private String programOznaka;
    private String uloga;
    private Long skolskaGodinaId;
    private String skolskaGodinaNaziv;

    @Override
    public String toString() {
        return "[" + id + "] " + predmetNaziv + " — " + nastavnikImePrezime;
    }
}
