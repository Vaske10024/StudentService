package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
public class IspitCreateRequest {
    @NotNull private Long rokId;
    @NotNull private Long drziPredmetId;
    @NotNull private LocalDate datum;
    @NotNull private LocalTime vreme;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime cancellationEnd;
}
