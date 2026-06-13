package org.raflab.studsluzba.model.dtos;


import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class DrziPredmetNewRequest {

    private Long predmetId;
    @NotEmpty
    private String predmetNaziv;
    @NotEmpty
    private String emailNastavnik;
}
