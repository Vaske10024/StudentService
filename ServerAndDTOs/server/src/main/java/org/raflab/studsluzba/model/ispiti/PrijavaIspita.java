package org.raflab.studsluzba.model.ispiti;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.raflab.studsluzba.model.StudentIndeks;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class PrijavaIspita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    @ToString.Include
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_indeks_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private StudentIndeks student;

    @ToString.Include
    private LocalDate datumPrijave;

    private String napomena;
    private Boolean ponisteno = false;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private PrijavaStatus status = PrijavaStatus.PRIJAVLJEN;
    private LocalDateTime cancelledAt;
    private Long cancelledByUserId;
    @Column(length = 1000)
    private String cancellationReason;
    private boolean daLiJeIzasao;

    private Integer brojOsvojenihPoena;
    private int ocena;

    private boolean priznatSDrugogFakulteta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ispit_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Ispit ispit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "predmet_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Predmet predmet;
}
