package org.raflab.studsluzba.model.dtos;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@NoArgsConstructor
public class PolozenPredmetDTO {
    private Long predmetId;
    private String sifra;
    private String naziv;
    private Integer espb;     // <<< DODATO (ako backend šalje)
    private Integer ocena;
    private String nacin;     // "ISPIT" ili "PRIZNAT"
    private LocalDate datum;
}
