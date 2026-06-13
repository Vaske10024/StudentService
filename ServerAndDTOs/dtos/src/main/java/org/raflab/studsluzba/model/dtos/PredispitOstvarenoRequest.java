package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;



@Data
public class PredispitOstvarenoRequest {
    @NotNull
    private Long studentIndeksId;
    @NotNull private Long predispitnaObavezaId;
    @Min(0) private int poeni;
}
