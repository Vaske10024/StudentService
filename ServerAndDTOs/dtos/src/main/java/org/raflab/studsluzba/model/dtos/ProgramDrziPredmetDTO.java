package org.raflab.studsluzba.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramDrziPredmetDTO {
    private Long id;                 // ID veze na programu
    private Long programId;          // opcionalno (ako backend šalje)
    private Integer semestar;        // 1..8

    private DrziPredmetDTO drziPredmet;
}
