package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.StudentIndeks;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.LockModeType;

import java.util.List;

@Repository
public interface StudentIndeksRepository extends JpaRepository<StudentIndeks, Long> {

	@Query("select indeks from StudentIndeks indeks " +
			"where indeks.studProgramOznaka = :studProgramOznaka " +
			"and indeks.godina = :godina " +
			"and indeks.broj = :broj")
	StudentIndeks findStudentIndeks(@Param("studProgramOznaka") String studProgramOznaka,
									@Param("godina") int godina,
									@Param("broj") int broj);

	@Query("select indeks from StudentIndeks indeks where " +
			"(:ime is null or lower(indeks.student.ime) like lower(concat('%', :ime, '%'))) and " +
			"(:prezime is null or lower(indeks.student.prezime) like lower(concat('%', :prezime, '%'))) and " +
			"(:studProgramOznaka is null or lower(indeks.studProgramOznaka) = lower(:studProgramOznaka)) and " +
			"(:godina is null or indeks.godina = :godina) and " +
			"(:broj is null or indeks.broj = :broj)")
	Page<StudentIndeks> findStudentIndeks(@Param("ime") String ime,
										  @Param("prezime") String prezime,
										  @Param("studProgramOznaka") String studProgramOznaka,
										  @Param("godina") Integer godina,
										  @Param("broj") Integer broj,
										  Pageable pageable);

	@Query("select si from StudentIndeks si where si.student.id = :idStudentPodaci")
	List<StudentIndeks> findStudentIndeksiForStudentPodaciId(@Param("idStudentPodaci") Long idStudentPodaci);

	@Query("select si from StudentIndeks si where si.student.id = :idStudentPodaci and si.aktivan = true")
	StudentIndeks findAktivanStudentIndeksiByStudentPodaciId(@Param("idStudentPodaci") Long idStudentPodaci);

	@Query("SELECT s.broj FROM StudentIndeks s WHERE s.godina = :godina AND s.studProgramOznaka = :studProgramOznaka ORDER BY s.broj ASC")
	List<Integer> findBrojeviByGodinaAndStudProgramOznaka(@Param("godina") int godina,
														  @Param("studProgramOznaka") String studProgramOznaka);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("SELECT s FROM StudentIndeks s WHERE s.godina = :godina AND s.studProgramOznaka = :studProgramOznaka ORDER BY s.broj ASC")
	List<StudentIndeks> lockIndeksiForNumberAllocation(@Param("godina") int godina,
												   @Param("studProgramOznaka") String studProgramOznaka);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select si from StudentIndeks si where si.id = :id")
	StudentIndeks findByIdForUpdate(@Param("id") Long id);

	// NOVO: gasi sve indekse (aktivne) za datog studenta
	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Transactional
	@Query("update StudentIndeks si set si.aktivan = false where si.student.id = :studentId and si.aktivan = true")
	int deactivateAllForStudent(@Param("studentId") Long studentId);
}
