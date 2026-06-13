package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class ProgramDrziPredmetCreateRequest {

    @NotNull
    private Long drziPredmetId;

    @NotNull
    @Min(1)
    @Max(8)
    private Integer semestar; // 1..8
}
