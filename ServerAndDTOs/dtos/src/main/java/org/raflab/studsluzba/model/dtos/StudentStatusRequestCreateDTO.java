package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
public class StudentStatusRequestCreateDTO {
    @NotNull
    private Long indeksId;

    @NotBlank
    private String type;

    @NotBlank
    private String reason;

    private LocalDate requestedFrom;
    private LocalDate requestedTo;
}
