package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
@Data
public class DrziPredmetCreateRequest {
    private Long predmetId;
    @NotNull private Long nastavnikId;
    private Long skolskaGodinaId; // ako null -> aktivna
    private Long realizacijaPredmetaId;
    private String uloga;
}
