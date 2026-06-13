package org.raflab.studsluzba.model.ispiti;

import javax.persistence.*;

import javax.validation.constraints.NotNull;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

@Entity
@Data
@ToString()
public class Predmet {
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(unique = true)
	private String sifra;

	private String naziv;
	private String opis;
	private Integer espb;

	@ManyToOne
	private StudijskiProgram studProgram;

	private boolean obavezan;


	@OneToMany(mappedBy = "predmet")
	private Set<PredispitnaObaveza> predispitneObaveze;






	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((sifra == null) ? 0 : sifra.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Predmet other = (Predmet) obj;
		if (sifra == null) {
			if (other.sifra != null)
				return false;
		} else if (!sifra.equals(other.sifra))
			return false;
		return true;
	}

}