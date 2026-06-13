package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.dtos.NastavnikLiteDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NastavnikRepository extends JpaRepository<Nastavnik, Long> {

	@Query("select n from Nastavnik n " +
			"where (:ime is null or lower(n.ime) like lower(concat('%', :ime, '%'))) and " +
			"(:prezime is null or lower(n.prezime) like lower(concat('%', :prezime, '%')))")
	List<Nastavnik> findByImeAndPrezime(@Param("ime") String ime,
										@Param("prezime") String prezime);

	List<Nastavnik> findByEmailIn(List<String> emails);
	boolean existsByEmailIgnoreCase(String email);
	boolean existsByJmbg(String jmbg);

	@Query("select new org.raflab.studsluzba.model.dtos.NastavnikLiteDTO(n.id, n.ime, n.prezime, n.email) from Nastavnik n")
	List<NastavnikLiteDTO> findAllLite();

	@Query("select new org.raflab.studsluzba.model.dtos.NastavnikLiteDTO(n.id, n.ime, n.prezime, n.email) from Nastavnik n where (:ime is null or lower(n.ime) like lower(concat('%', :ime, '%'))) and (:prezime is null or lower(n.prezime) like lower(concat('%', :prezime, '%')))  ")
	List<NastavnikLiteDTO> searchLite(@Param("ime") String ime, @Param("prezime") String prezime);

}
