package org.raflab.studsluzba.model.ispiti;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
@Data
@Entity

@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_rok_sg_period",
                        columnNames = {"skolska_godina_id", "datumPocetka", "datumZavrsetka"}
                )
        }
)
public class IspitniRok {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate datumPocetka;


    private LocalDate datumZavrsetka;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime cancellationEnd;
    private boolean active = true;

    @OneToMany(mappedBy = "ispitniRok")
    private Set<Ispit> ispiti;

    @ManyToOne
    @JoinColumn(name = "skolska_godina_id")
    private SkolskaGodina skolskaGodina;
}
