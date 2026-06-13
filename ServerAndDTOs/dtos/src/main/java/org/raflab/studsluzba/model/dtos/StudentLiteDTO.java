package org.raflab.studsluzba.model.dtos;


import lombok.Data;

@Data
public class StudentLiteDTO {
    private Long indeksId;
    private String studProgramOznaka;
    private int godina;
    private int broj;

    private Long studentId;
    private String ime;
    private String prezime;
}
