package org.raflab.studsluzba.model.ispiti;


import lombok.Data;
import org.raflab.studsluzba.model.StudentIndeks;

import javax.persistence.*;

@Entity
@Data
@Table(uniqueConstraints=@UniqueConstraint(columnNames={"student_indeks_id","predispitna_obaveza_id"}))

public class OstvarenaPredObav {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;

    @ManyToOne(optional=false) @JoinColumn(name="student_indeks_id")
    private StudentIndeks student;

    @ManyToOne(optional=false) @JoinColumn(name="predispitna_obaveza_id")
    private PredispitnaObaveza obaveza;

    private int osvojeniPoeni;
}