package org.raflab.studsluzba.model.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.Set;

@Data
@NoArgsConstructor
public class NastavnikResponse {
    @NonNull
    private Long id;
    @NonNull
    private String ime;

    private String prezime;
    private String srednjeIme;
    private String email;
    private String brojTelefona;
    private String adresa;

    // DTO umesto entiteta
    private Set<NastavnikZvanjeDTO> zvanja;

    private LocalDate datumRodjenja;
    private Character pol;
    private String jmbg;
}
