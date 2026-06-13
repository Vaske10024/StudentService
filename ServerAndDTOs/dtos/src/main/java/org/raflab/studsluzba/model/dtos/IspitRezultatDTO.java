package org.raflab.studsluzba.model.dtos;

import lombok.Data;

@Data
public class IspitRezultatDTO {
    private StudentLiteDTO student;
    private int ukupniPoeni;
    private int ocena;
    private boolean izasao;
    private boolean ponisteno;
    private String napomena;
}
