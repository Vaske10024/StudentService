package org.raflab.studsluzba.model.dtos;


import lombok.Data;

@Data
public class StudentDTO {

    private Long idIndeks;
    private Long idStudentPodaci;
    // dodati jos nesto?
    private String ime;
    private String prezime;
    // indeks
    private int godinaUpisa;
    private String studProgramOznaka;
    private int broj;
    private boolean aktivanIndeks;
}