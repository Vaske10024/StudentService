package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Data
public class IspitniRokCreateRequest {
    @NotNull
    private LocalDate start;
    @NotNull private LocalDate end;
    private Long skolskaGodinaId; // optional
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime cancellationEnd;
    private boolean active = true;
}
