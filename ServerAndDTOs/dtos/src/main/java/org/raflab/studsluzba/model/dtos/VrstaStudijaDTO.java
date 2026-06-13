package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class VrstaStudijaDTO {
    private Long id;
    private String skracenica;
    private String puniNaziv;
}
