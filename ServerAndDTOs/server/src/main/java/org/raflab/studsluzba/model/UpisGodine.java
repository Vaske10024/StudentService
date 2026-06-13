package org.raflab.studsluzba.model;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_upis_indeks_godina_sg",
        columnNames = {"indeks_id", "upisuje_godinu", "skolska_godina_id"}))
public class UpisGodine {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private StudentIndeks indeks;
    @Column(name = "upisuje_godinu")
    private int upisujeGodinu;               // 1..N
    private LocalDate datum;
    private String napomena;
    @ManyToOne(optional=false)
    @JoinColumn(name = "skolska_godina_id")
    private SkolskaGodina skolskaGodina;
}
