package org.raflab.studsluzba.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;

@Entity
@Data
public class ObnovaGodine {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private StudentIndeks indeks;
    private int obnavljaGodinu;
    private LocalDate datum;
    private String napomena;
    @ManyToOne
    @JoinColumn(name = "skolska_godina_id")
    private SkolskaGodina skolskaGodina;
    @ManyToOne
    @JoinColumn(name = "upis_godine_id")
    private UpisGodine upisGodine;
}
