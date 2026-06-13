package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
public class IspitUpdateTimeRequest {
    private LocalDate datum;
    private LocalTime vreme;
}
