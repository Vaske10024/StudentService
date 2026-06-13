package org.raflab.studsluzba.model.dtos;


import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
public class PrijavaResponseDTO {
    private Long id;
    private Long ispitId;
    private Long predmetId;
    private String predmetSifra;
    private String predmetNaziv;
    private String nastavnikImePrezime;
    private LocalDate datumIspita;
    private LocalTime vremePocetka;
    private LocalDate datumPrijave;

    private StudentLiteDTO student;
    private Integer predispitniPoeni;
    private Integer ispitniPoeni;
    private Integer ukupnoPoena;
    private Integer ocena;
    private boolean izasao;
    private boolean ponisteno;
    private String napomena;
    private String status;
    private LocalDateTime cancelledAt;
    private String cancellationReason;
}
