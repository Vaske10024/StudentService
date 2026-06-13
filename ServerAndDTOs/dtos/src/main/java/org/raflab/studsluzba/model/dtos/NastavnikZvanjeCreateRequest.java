package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;


@Data
public class NastavnikZvanjeCreateRequest {
    @NotNull
    private Long nastavnikId;
    @NotBlank
    private String zvanje;
    private String naucnaOblast;
    private String uzaNaucnaOblast;
    @NotNull private LocalDate datumIzbora;
    private Boolean aktivno = true;
}
