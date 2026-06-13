package org.raflab.studsluzba.model.ispiti;

import lombok.Data;
import org.raflab.studsluzba.model.Nastavnik;

import javax.persistence.*;



/*
  Ovo je kao klasa odrzavanje predmeta, i tehnicki moze
  da se u skolsku godinu ubaci ovo
*/
@Entity
@Data
@Table(
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_drzi_realizacija_nastavnik_uloga",
                        columnNames = {"realizacija_predmeta_id", "nastavnik_id", "uloga"}
                )
        }
)
public class DrziPredmet {

    public enum Uloga {
        NOSILAC, PREDAVANJA, VEZBE, PRAKTIKUM
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "nastavnik_id")  // Povezuje sa nastavnikom
    private Nastavnik nastavnik;

    @ManyToOne
    @JoinColumn(name = "predmet_id")  // Povezuje sa predmetom
    private Predmet predmet;

    @ManyToOne
    @JoinColumn(name = "skolska_godina_id")  // Povezuje sa školskom godinom
    private SkolskaGodina skolskaGodina;

    @ManyToOne
    @JoinColumn(name = "realizacija_predmeta_id")
    private RealizacijaPredmeta realizacijaPredmeta;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Uloga uloga = Uloga.NOSILAC;
}
