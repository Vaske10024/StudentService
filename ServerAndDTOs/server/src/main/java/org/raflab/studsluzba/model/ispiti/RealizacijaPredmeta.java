package org.raflab.studsluzba.model.ispiti;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.ProgramPredmet;

import javax.persistence.*;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "realizacija_predmeta",
        uniqueConstraints = @UniqueConstraint(name = "uk_realizacija_program_predmet_sg",
                columnNames = {"program_predmet_id", "skolska_godina_id"}))
public class RealizacijaPredmeta {

    public enum Status {
        DRAFT, ACTIVE, CLOSED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "program_predmet_id")
    private ProgramPredmet programPredmet;

    @ManyToOne(optional = false)
    @JoinColumn(name = "skolska_godina_id")
    private SkolskaGodina skolskaGodina;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Status status = Status.DRAFT;

    @OneToMany(mappedBy = "realizacijaPredmeta")
    private Set<DrziPredmet> angazovanja;

    @OneToMany(mappedBy = "realizacijaPredmeta")
    private Set<SlusaPredmet> slusanja;
}
