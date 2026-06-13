package org.raflab.studsluzba.repositories;

import java.util.List;
import java.util.Optional;

import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PredmetRepository extends CrudRepository<Predmet, Long> {
	@Query("select p from Predmet p where p.studProgram.godinaAkreditacije = :godinaAkreditacije")
	List<Predmet> getPredmetForGodinaAkreditacije(Integer godinaAkreditacije);

	List<Predmet> getPredmetsByStudProgramAndObavezan(StudijskiProgram studProgram, boolean obavezan);


	Optional<Predmet> findBySifra(String sifra);

	List<Predmet> findByIdIn(List<Long> ids);
	List<Predmet> findByNazivIn(List<String> nazivi);

}
