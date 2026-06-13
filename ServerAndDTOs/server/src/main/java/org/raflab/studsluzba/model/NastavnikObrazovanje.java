package org.raflab.studsluzba.model;


import lombok.Data;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Data
public class NastavnikObrazovanje {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    private Nastavnik nastavnik;

    private String nivo;   // Osnovne, Master, Doktorske...
    private String ustanova;
    private String mesto;
    private LocalDate datumDiplomiranja;
}
