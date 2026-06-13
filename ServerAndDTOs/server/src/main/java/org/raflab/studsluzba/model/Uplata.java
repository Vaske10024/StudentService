package org.raflab.studsluzba.model;

import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Data
public class Uplata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    private StudentIndeks indeks;

    private LocalDate datum;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal iznosRsd;

    @Column(nullable = false, precision = 19, scale = 6)
    private BigDecimal srednjiKursEur;

    @Column(nullable = false)
    private boolean fallbackKurs;
}
