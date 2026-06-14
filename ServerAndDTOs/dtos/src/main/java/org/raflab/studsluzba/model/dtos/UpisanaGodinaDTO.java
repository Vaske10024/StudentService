package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
public class UpisanaGodinaDTO {
    private Long id;          // id zapisa (UpisGodine ili ObnovaGodine)
    private String tip;       // "UPIS" ili "OBNOVA"
    private int godina;       // upisujeGodinu / obnavljaGodinu
    private LocalDate datum;
    private String napomena;
    private String skolskaGodina;

    public UpisanaGodinaDTO(Long id, String tip, int godina, LocalDate datum, String napomena) {
        this(id, tip, godina, datum, napomena, null);
    }

    public UpisanaGodinaDTO(Long id, String tip, int godina, LocalDate datum, String napomena, String skolskaGodina) {
        this.id = id;
        this.tip = tip;
        this.godina = godina;
        this.datum = datum;
        this.napomena = napomena;
        this.skolskaGodina = skolskaGodina;
    }
}
