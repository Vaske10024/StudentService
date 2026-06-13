package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class ObnovaCreateRequest {
    @NotNull
    private Long indeksId;

    @NotNull
    private List<Long> drziPredmetIds;

    // NOVO: obavezno – koja godina studija se obnavlja (1..4)
    @NotNull
    @Min(1)
    @Max(4)
    private Integer obnavljaGodinu;
}
