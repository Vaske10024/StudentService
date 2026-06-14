package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.Ispit;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;

public interface IspitRepository extends CrudRepository<Ispit, Long> {
    List<Ispit> findByIspitniRokId(Long ispitniRokId);

    @EntityGraph(attributePaths = {"ispitniRok", "drziPredmet", "drziPredmet.predmet", "drziPredmet.nastavnik"})
    @Query("select distinct i from Ispit i where exists (" +
            "select sp.id from SlusaPredmet sp where sp.studentIndeks.id = :studentIndeksId " +
            "and sp.skolskaGodina.aktivna = true and sp.realizacijaPredmeta.programPredmet.predmet.id = i.drziPredmet.predmet.id) " +
            "order by i.datumOdrzavanja asc, i.vremePocetka asc")
    List<Ispit> findCandidatesForStudent(@Param("studentIndeksId") Long studentIndeksId);

    @Query("select i from Ispit i where i.drziPredmet.nastavnik.id = :nastavnikId order by i.datumOdrzavanja desc")
    List<Ispit> findByNastavnikId(@Param("nastavnikId") Long nastavnikId);

    @Query("select case when count(i)>0 then true else false end from Ispit i " +
            "where i.zakljucen = true and i.drziPredmet.predmet.id = :predmetId and i.ispitniRok.skolskaGodina.id = :skolskaGodinaId")
    boolean existsLockedForPredmetAndSkolskaGodina(@Param("predmetId") Long predmetId,
                                                   @Param("skolskaGodinaId") Long skolskaGodinaId);
}
