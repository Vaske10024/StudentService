package org.raflab.studsluzba.model;

import lombok.Data;

import javax.persistence.*;

@Entity
@Data
public class VrstaStudija {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, unique=true)
    private String skracenica;
    @Column(nullable=false)
    private String puniNaziv;
}