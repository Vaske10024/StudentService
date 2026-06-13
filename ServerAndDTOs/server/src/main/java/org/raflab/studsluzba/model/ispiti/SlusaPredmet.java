package org.raflab.studsluzba.model.ispiti;

import lombok.Data;

import org.raflab.studsluzba.model.StudentIndeks;

import javax.persistence.*;

@Entity
@Data
@Table(name = "slusa_predmet",
        uniqueConstraints = @UniqueConstraint(name = "uk_slusa_student_realizacija",
                columnNames = {"student_indeks_id", "realizacija_predmeta_id"}))
public class SlusaPredmet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(optional=false)
    @JoinColumn(name = "student_indeks_id")
    private StudentIndeks studentIndeks;


    @ManyToOne
    @JoinColumn(name = "drzi_predmet_id")
    private DrziPredmet drziPredmet;

    @ManyToOne(optional=false)
    @JoinColumn(name = "realizacija_predmeta_id")
    private RealizacijaPredmeta realizacijaPredmeta;

    @ManyToOne
    @JoinColumn(name = "upis_godine_id")
    private org.raflab.studsluzba.model.UpisGodine upisGodine;

    @ManyToOne(optional=false)
    private SkolskaGodina skolskaGodina;
}
