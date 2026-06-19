package org.raflab.studsluzba.model.dtos;

import lombok.Data;

@Data
public class IspitRezultatDTO {
    private Long id;
    private Long ispitId;
    private StudentLiteDTO student;
    private Integer predispitniPoeni;
    private Integer ispitniPoeni;
    private int ukupniPoeni;
    private int ocena;
    private boolean izasao;
    private boolean ponisteno;
    private String napomena;
}
