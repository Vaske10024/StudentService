package org.raflab.studsluzba.model.dtos;

import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class IspitniRokDTO {
    private Long id;
    private LocalDate datumPocetka;
    private LocalDate datumZavrsetka;

    // DODATO:
    private Long skolskaGodinaId;
    private LocalDateTime registrationStart;
    private LocalDateTime registrationEnd;
    private LocalDateTime cancellationEnd;
    private boolean active;

    @Override
    public String toString() {
        String p = datumPocetka == null ? "?" : datumPocetka.toString();
        String k = datumZavrsetka == null ? "?" : datumZavrsetka.toString();
        return "Rok #" + (id == null ? "?" : id) + " (" + p + " - " + k + ")"
                + (skolskaGodinaId != null ? " • SG=" + skolskaGodinaId : "");
    }
}
