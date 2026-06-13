package org.raflab.studsluzba.model.dtos;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.Set;

@Data

@NoArgsConstructor
public class NastavnikRequest {
    @NonNull
    private String ime;
    @NonNull
    private String prezime;
    @NonNull
    private String srednjeIme;
    @NonNull
    private String email;

    private String brojTelefona;
    private String adresa;

    // DTO umesto entiteta (opciono; mi ga ignorišemo u Converters.toNastavnik)
    private Set<NastavnikZvanjeDTO> zvanja;

    private LocalDate datumRodjenja;
    private Character pol;
    private String jmbg;
}
