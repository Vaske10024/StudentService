package org.raflab.studsluzba.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class ObnovaGodine {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private StudentIndeks indeks;
    private int obnavljaGodinu;
    private LocalDate datum;
    private String napomena;
}