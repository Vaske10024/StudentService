package org.raflab.studsluzba.model.ispiti;

import lombok.Data;

import javax.persistence.*;


@Data
@Entity


@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_predob_predmet_sg_vrsta",
                        columnNames = {"predmet_id", "skolska_godina_id", "vrsta"}
                )
        }
)
public class PredispitnaObaveza {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private String vrsta;

    private int maxPoeni;

    @ManyToOne
    private Predmet predmet;

    @ManyToOne(optional=false) @JoinColumn(name="skolska_godina_id")
    private SkolskaGodina skolskaGodina;

}
