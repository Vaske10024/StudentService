package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

@Data
public class UpisCreateRequest {
    @NotNull
    private Long indeksId;

    private List<Long> drziPredmetIds;

    // NOVO (opciono): ako proslediš, upis ide baš na tu godinu (1..4),
    // ako je null -> računa se kao do sada (sledeća po UpisGodine)
    @Min(1)
    @Max(4)
    private Integer upisujeGodinu;
    private boolean adminOverride;
    private String overrideReason;
}
