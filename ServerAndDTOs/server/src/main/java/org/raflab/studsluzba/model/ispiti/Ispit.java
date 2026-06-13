package org.raflab.studsluzba.model.ispiti;

import lombok.Data;
import org.raflab.studsluzba.model.Nastavnik;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

@Entity
@Data
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ispit_drzi_predmet_datum",
                        columnNames = {"drzi_predmet_id", "datum_odrzavanja"}
                )
        }
)
public class Ispit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datum_odrzavanja")
    private LocalDate datumOdrzavanja;

    @ManyToOne
    @JoinColumn(name = "ispitni_rok_id")
    private IspitniRok ispitniRok;

    @ManyToOne
    @JoinColumn(name = "drzi_predmet_id")
    private DrziPredmet drziPredmet;

    // ✅ Baza traži nastavnik_id (NOT NULL)
    @ManyToOne
    @JoinColumn(name = "nastavnik_id")
    private Nastavnik nastavnik;

    // ✅ Baza traži predmet_id (NOT NULL)
    @ManyToOne
    @JoinColumn(name = "predmet_id")
    private Predmet predmet;

    @Column(name = "vreme_pocetka")
    private LocalTime vremePocetka;

    @OneToMany(mappedBy = "ispit", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PrijavaIspita> prijave;

    private boolean zakljucen;
}
