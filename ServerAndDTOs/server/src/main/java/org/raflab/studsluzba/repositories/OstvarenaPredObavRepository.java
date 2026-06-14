package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.OstvarenaPredObav;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.List;

@Repository
public interface OstvarenaPredObavRepository extends CrudRepository<OstvarenaPredObav, Long> {

    @Query("select sum(opo.osvojeniPoeni) from OstvarenaPredObav opo " +
            "where opo.student.id = :studentIndeksId and opo.obaveza.predmet.id = :predmetId " +
            "and opo.obaveza.skolskaGodina.id = :skolskaGodinaId")
    Integer ostvareniPredispitniPoeni(@Param("studentIndeksId") Long studentIndeksId,
                                      @Param("predmetId") Long predmetId,
                                      @Param("skolskaGodinaId") Long skolskaGodinaId);

    @Query("select o from OstvarenaPredObav o where o.student.id = :studentId and o.obaveza.id = :obavezaId")
    Optional<OstvarenaPredObav> findByStudentAndObaveza(@Param("studentId") Long studentId,
                                                        @Param("obavezaId") Long obavezaId);

    List<OstvarenaPredObav> findByObavezaId(Long obavezaId);
}
