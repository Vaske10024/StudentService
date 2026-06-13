package org.raflab.studsluzba.model.ispiti;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.VrstaStudija;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_program_oznaka_godina", columnNames = {"oznaka", "godinaAkreditacije"})
        }
)
public class StudijskiProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String oznaka;  // RN, RM
    private String naziv;
    private Integer godinaAkreditacije;
    private String zvanje;
    private Integer trajanjeGodina;
    private Integer trajanjeSemestara;

    @ManyToOne(optional = false)
    private VrstaStudija vrstaStudija; // OAS, OSS, MAS...

    private Integer ukupnoEspb;

    @JsonIgnore
    @OneToMany(mappedBy = "studProgram")
    private List<Predmet> predmeti;

    @JsonIgnore
    @OneToMany(mappedBy = "studijskiProgram")
    private Set<StudentIndeks> studenti;
}
