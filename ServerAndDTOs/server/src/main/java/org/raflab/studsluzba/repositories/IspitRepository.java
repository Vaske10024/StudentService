package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.Ispit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface IspitRepository extends CrudRepository<Ispit, Long> {
    List<Ispit> findByIspitniRokId(Long ispitniRokId);

    @Query("select i from Ispit i where i.drziPredmet.nastavnik.id = :nastavnikId order by i.datumOdrzavanja desc")
    List<Ispit> findByNastavnikId(@Param("nastavnikId") Long nastavnikId);

    @Query("select case when count(i)>0 then true else false end from Ispit i " +
            "where i.zakljucen = true and i.drziPredmet.predmet.id = :predmetId and i.ispitniRok.skolskaGodina.id = :skolskaGodinaId")
    boolean existsLockedForPredmetAndSkolskaGodina(@Param("predmetId") Long predmetId,
                                                   @Param("skolskaGodinaId") Long skolskaGodinaId);
}
