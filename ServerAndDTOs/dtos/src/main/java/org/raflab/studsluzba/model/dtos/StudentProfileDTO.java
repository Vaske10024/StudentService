package org.raflab.studsluzba.model.dtos;

/*
 * objekat ove kalse sadrzi sve podatke o studentu koji
 * se prikazuju u njegovom profilu
 *
 * - polozeni predmeti
 * - tok studija (upisi, obnove godina)
 * - predmete koje slusa
 * - prijavljeni ispiti
 * - uplate
 *
 * - selektujemo preko indeksa, potrebno prikupiti podatke i o drugim indeksima
 *
 */

import lombok.Data;

import java.util.List;


import lombok.Data;

import java.util.List;

@Data
public class StudentProfileDTO {
    private StudentIndeksResponse indeks;

    // DTO umesto entiteta
    private List<SlusaPredmetDTO> slusaPredmete;

    // DTO umesto entiteta
    private List<PredmetDTO> nepolozeniPredmeti;
}
