package org.raflab.studsluzba.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.raflab.studsluzba.model.ispiti.TokStudija;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.Set;

@Entity
@Data
@Table(
		uniqueConstraints = {
				@UniqueConstraint(name = "uk_student_jmbg", columnNames = {"jmbg"}),
				@UniqueConstraint(name = "uk_student_email_fak", columnNames = {"emailFakultetski"}),
				@UniqueConstraint(name = "uk_student_email_priv", columnNames = {"emailPrivatni"}),
				@UniqueConstraint(name = "uk_student_broj_lk", columnNames = {"brojLicneKarte"})
		}
)
public class StudentPodaci {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String ime;
	private String prezime;
	private String srednjeIme;
	private String jmbg;
	private LocalDate datumRodjenja;
	private String mestoRodjenja;
	private String mestoPrebivalista;
	private String drzavaRodjenja;
	private String drzavljanstvo;
	private String nacionalnost;
	private Character pol;
	private String adresa;
	private String brojTelefonaMobilni;
	private String brojTelefonaFiksni;
	private String emailFakultetski;
	private String emailPrivatni;
	private String brojLicneKarte;
	private String licnuKartuIzdao;
	private String mestoStanovanja;
	private String adresaStanovanja;

	// ovo sedi jer smo kasnije dodali klasu visokaskola i to malo rusi neke bitne funkcije a nije bitna stvar
	private String visokaSkolaPrelazak;

	@ManyToOne
	@JoinColumn(name = "visoka_skola_prelazak_id")
	private VisokaSkola visokaSkolaPrelazakRef;

	@ManyToOne
	@JoinColumn(name = "srednja_skola_id")
	private SrednjaSkola srednjaSkola;

	@JsonIgnore
	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
	private Set<StudentIndeks> indeksi;

	@JsonIgnore
	@OneToOne(mappedBy = "studentPodaci", cascade = CascadeType.ALL, orphanRemoval = true)
	private TokStudija tokStudija;

	private Float uspehIzSredjeSkole;
	private Float uspehSaPrijemnog;
}
