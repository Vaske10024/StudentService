package org.raflab.studsluzba.model;

import lombok.Data;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;

import javax.persistence.*;

@Entity
@Data
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_program_predmet", columnNames = {"program_id","predmet_id"}))
public class ProgramPredmet {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional=false)
    @JoinColumn(name="program_id")
    private StudijskiProgram program;

    @ManyToOne(optional=false)
    @JoinColumn(name="predmet_id")
    private org.raflab.studsluzba.model.ispiti.Predmet predmet;

    // NOVO: 1..4
    @Column(nullable = false)
    private Integer godinaStudija;

    // NOVO: 1..2 (semestar u okviru godine)
    @Column(nullable = false)
    private Integer semestarUGodini;

    private Integer fondPredavanja;
    private Integer fondVezbi;
    private Integer fondPraktikum;

    // pomoćno (nije kolona): 1..8
    @Transient
    public Integer getSemestarUkupno() {
        if (godinaStudija == null || semestarUGodini == null) return null;
        return (godinaStudija - 1) * 2 + semestarUGodini;
    }
}
