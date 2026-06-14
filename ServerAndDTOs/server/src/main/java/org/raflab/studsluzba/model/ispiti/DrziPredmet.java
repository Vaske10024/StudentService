package org.raflab.studsluzba.model.ispiti;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.Nastavnik;

import javax.persistence.*;



/*
  Ovo je kao klasa odrzavanje predmeta, i tehnicki moze
  da se u skolsku godinu ubaci ovo
*/
@Entity
@Getter
@Setter
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof DrziPredmet)) return false;
        DrziPredmet that = (DrziPredmet) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
