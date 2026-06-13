package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpisanaGodinaDTO {
    private Long id;          // id zapisa (UpisGodine ili ObnovaGodine)
    private String tip;       // "UPIS" ili "OBNOVA"
    private int godina;       // upisujeGodinu / obnavljaGodinu
    private LocalDate datum;
    private String napomena;
}
