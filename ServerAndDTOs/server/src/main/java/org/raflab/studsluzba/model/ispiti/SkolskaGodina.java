package org.raflab.studsluzba.model.ispiti;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Set;

@Getter
@Setter
@Entity
public class SkolskaGodina {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @Column(nullable = false, unique = true)
    private String godina;  //25/26

    private boolean aktivna;

    @OneToMany(mappedBy = "skolskaGodina", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<DrziPredmet> predmeti;

    @OneToMany(mappedBy = "skolskaGodina", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<SlusaPredmet> listaSlusanjaPredmeta;

    @OneToMany(mappedBy = "skolskaGodina")
    private Set<IspitniRok> ispitniRokovi;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SkolskaGodina)) return false;
        SkolskaGodina that = (SkolskaGodina) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
