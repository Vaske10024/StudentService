package org.raflab.studsluzba.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

import javax.persistence.*;
import java.util.List;

@Entity
@Data
@Table(
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_srednja_naziv_mesto", columnNames = {"naziv", "mesto"})
        }
)
public class SrednjaSkola {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String naziv;

    private String mesto;

    private String vrsta;

    @JsonIgnore
    @OneToMany(mappedBy = "srednjaSkola", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StudentPodaci> studenti;
}
