package org.raflab.studsluzba.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_visoka_naziv_mesto", columnNames = {"naziv", "mesto"})
})
public class VisokaSkola {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) private String naziv;
    @Column(nullable = false) private String mesto;
    private String tip; // univerzitet / visoka škola / fakultet...
}
