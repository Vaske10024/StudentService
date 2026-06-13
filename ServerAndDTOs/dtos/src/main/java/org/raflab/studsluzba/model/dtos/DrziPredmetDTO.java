package org.raflab.studsluzba.model.dtos;

import lombok.Data;


@Data
public class DrziPredmetDTO {
    private Long id;

    private Long predmetId;
    private String predmetNaziv;

    private Long nastavnikId;
    private String nastavnikImePrezime;
    private Long realizacijaPredmetaId;
    private String uloga;

    @Override
    public String toString() {
        return (predmetNaziv == null ? "?" : predmetNaziv)
                + " — " + (nastavnikImePrezime == null ? "?" : nastavnikImePrezime)
                + " (DP#" + id + ")";
    }
}
