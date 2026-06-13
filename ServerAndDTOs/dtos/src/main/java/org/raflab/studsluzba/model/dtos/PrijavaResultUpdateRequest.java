package org.raflab.studsluzba.model.dtos;


import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class PrijavaResultUpdateRequest {
    @NotNull
    private Long prijavaId;

    // Poeni na ispitu (SAMO ispit); predispit se sabira iz baze
    @Min(0) @Max(100)
    private Integer brojOsvojenihPoena;

    // Flag da li je student izasao
    private Boolean izasao;

    // (opciono) rucni unos ocene – ako nije prosleđeno, racuna se automatski
    @Min(5) @Max(10)
    private Integer ocena;

    private String napomena;
}
