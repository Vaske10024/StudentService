package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

@Data
public class IspitIzlazakRequest {

    @NotNull
    private Long prijavaId;

    @NotNull
    @Min(0)
    private Integer brojOsvojenihPoena;

    private String napomena;  // opciono
}
