package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data


public class IspitDTO {
    private Long id;
    private LocalDate datumOdrzavanja;
    private LocalTime vremePocetka;
    private boolean zakljucen;

    private Long ispitniRokId;
    private LocalDate rokDatumPocetka;
    private LocalDate rokDatumZavrsetka;
    private Long drziPredmetId;
    private String predmetNaziv;
    private String nastavnikImePrezime;

    @Override
    public String toString() {
        return "Ispit #" + id + " • " + datumOdrzavanja + " " + vremePocetka
                + " • " + (predmetNaziv == null ? "" : predmetNaziv)
                + (nastavnikImePrezime == null ? "" : " — " + nastavnikImePrezime)
                + (zakljucen ? " • ZAKLJUČEN" : "");
    }
}
