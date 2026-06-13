package org.raflab.studsluzba.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;
import java.util.Objects;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_nastavnik_email", columnNames = {"email"}),
				@UniqueConstraint(name = "uk_nastavnik_jmbg", columnNames = {"jmbg"})
		}
)
public class Nastavnik {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String ime;
	private String prezime;
	private String srednjeIme;
	private String email;
	private String brojTelefona;
	private String adresa;

	private String zavrseniFakultet;

	@JsonIgnore
	@OneToMany(mappedBy = "nastavnik", fetch = FetchType.LAZY)
	private Set<NastavnikZvanje> zvanja;

	private LocalDate datumRodjenja;
	private Character pol;
	private String jmbg;

	// equals/hashCode ONLY by id (best practice for JPA)
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Nastavnik)) return false;
		Nastavnik n = (Nastavnik) o;
		return id != null && id.equals(n.id);
	}

	@Override
	public int hashCode() {
		return 31;
	}
}
