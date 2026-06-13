package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import java.time.LocalDate;


@Data
public class StudentIndeksRequest {
    private Long id;
    private int broj;
    private int godina;
    private String studProgramOznaka;
    private String nacinFinansiranja;
    private boolean aktivan;
    private LocalDate vaziOd;

    private StudentPodaciResponse student;

    // DTO umesto entiteta
    private StudijskiProgramDTO studijskiProgram;

    private Integer ostvarenoEspb;
}