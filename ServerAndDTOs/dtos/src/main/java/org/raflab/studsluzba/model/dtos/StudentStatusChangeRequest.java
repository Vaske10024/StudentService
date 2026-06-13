package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentStatusChangeRequest {
    @NotNull
    private String newStatus;

    @NotBlank
    private String reason;

    private LocalDate validFrom;
    private LocalDate validTo;
}
