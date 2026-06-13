package org.raflab.studsluzba.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@ToString(onlyExplicitlyIncluded = true)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Table(
		uniqueConstraints = @UniqueConstraint(name = "uk_indeks_broj_godina_program",
				columnNames = {"broj", "godina", "studProgramOznaka"}
		)
)
public class StudentIndeks {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	@ToString.Include
	private Long id;

	@ToString.Include
	private Integer broj;

	@ToString.Include
	private Integer godina;

	@ToString.Include
	private String studProgramOznaka;

	private String nacinFinansiranja;
	private boolean aktivan;
	private LocalDate vaziOd;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private StudentStatus status;

	@Column(length = 1000)
	private String statusReason;

	private LocalDateTime activatedAt;
	private LocalDateTime deactivatedAt;

	@PrePersist
	void initializeStatus() {
		if (status == null) status = aktivan ? StudentStatus.AKTIVAN : StudentStatus.NEAKTIVAN;
	}

	@ManyToOne(fetch = FetchType.LAZY)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private StudentPodaci student;

	@ManyToOne(fetch = FetchType.LAZY)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private StudijskiProgram studijskiProgram;

	@OneToMany(mappedBy = "student", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private Set<PrijavaIspita> prijaveIspita;

	private Integer ostvarenoEspb;

	@OneToMany(mappedBy = "studentIndeks", cascade = CascadeType.ALL, orphanRemoval = true)
	@ToString.Exclude
	@EqualsAndHashCode.Exclude
	private List<SlusaPredmet> predmetiKojeSlusa;
}
