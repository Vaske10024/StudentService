package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.PredispitnaObaveza;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PredispitnaObavezaRepository extends CrudRepository<PredispitnaObaveza, Long> {

    @Query("select p from PredispitnaObaveza p where p.predmet.id = :predmetId and p.skolskaGodina.id = :skolskaGodinaId")
    List<PredispitnaObaveza> findByPredmetIdAndSkolskaGodinaId(@Param("predmetId") Long predmetId,
                                                               @Param("skolskaGodinaId") Long skolskaGodinaId);
}
