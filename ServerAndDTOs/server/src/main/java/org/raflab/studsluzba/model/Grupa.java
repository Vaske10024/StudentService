package org.raflab.studsluzba.model;

import lombok.Data;
import org.raflab.studsluzba.model.ispiti.Predmet;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

@Entity
@Data
public class Grupa {
	
	@Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private String naziv;

	@ManyToMany
	private List<Predmet> predmeti;
}
