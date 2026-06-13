package org.raflab.studsluzba.model.ispiti;

import lombok.Data;

import javax.persistence.*;
import java.util.Set;

@Data
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




}
