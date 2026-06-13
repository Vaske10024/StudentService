package org.raflab.studsluzba.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class NastavnikZvanje {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    private LocalDate datumIzbora;
    private String naucnaOblast;
    private String uzaNaucnaOblast;
    private String zvanje;
    private boolean aktivno;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    private Nastavnik nastavnik;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NastavnikZvanje)) return false;
        NastavnikZvanje nz = (NastavnikZvanje) o;
        return id != null && id.equals(nz.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }
}
