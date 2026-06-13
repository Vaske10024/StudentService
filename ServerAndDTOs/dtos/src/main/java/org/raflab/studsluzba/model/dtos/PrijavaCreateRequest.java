package org.raflab.studsluzba.model.dtos;


import lombok.Data;

import javax.validation.constraints.NotNull;

@Data
public class PrijavaCreateRequest {
    @NotNull
    private Long ispitId;
    @NotNull
    private Long studentIndeksId;

    // opcioni inicijalni podaci
    private String napomena;
}
