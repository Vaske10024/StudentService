package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DrziPredmetRepository extends CrudRepository<DrziPredmet, Long> {
    DrziPredmet findFirstByRealizacijaPredmetaIdAndNastavnikIdAndUloga(
            Long realizacijaPredmetaId, Long nastavnikId, DrziPredmet.Uloga uloga);

    @Query("select dp from DrziPredmet dp where dp.predmet.id = :predmetId and dp.nastavnik.id = :nastavnikId and dp.skolskaGodina.id = :sgId")
    DrziPredmet getDrziPredmetNastavnikPredmetUGodini(@Param("predmetId") Long predmetId,
                                                      @Param("nastavnikId") Long nastavnikId,
                                                      @Param("sgId") Long skolskaGodinaId);


    @Query("select dp\n" +
            "    from DrziPredmet dp\n" +
            "    where dp.predmet.id = :predmetId\n" +
            "      and dp.nastavnik.id = :nastavnikId\n" +
            "      and dp.skolskaGodina.aktivna = true"

)
    DrziPredmet getDrziPredmetNastavnikPredmet(
            @Param("predmetId") Long predmetId,
            @Param("nastavnikId") Long nastavnikId
    );

    @EntityGraph(attributePaths = {"predmet", "nastavnik", "realizacijaPredmeta", "realizacijaPredmeta.programPredmet", "realizacijaPredmeta.programPredmet.program"})
    Iterable<DrziPredmet> findAll();


    @Query("select dp from DrziPredmet dp " +
            "join fetch dp.predmet p " +
            "join fetch dp.nastavnik n " +
            "join fetch dp.skolskaGodina sg " +
            "left join fetch dp.realizacijaPredmeta r " +
            "left join fetch r.programPredmet pp " +
            "left join fetch pp.program prog " +
            "where sg.aktivna = true")
    List<DrziPredmet> findAllAktivnaWithRefs();

    @Query("select dp from DrziPredmet dp " +
            "join fetch dp.predmet p " +
            "join fetch dp.nastavnik n " +
            "join fetch dp.skolskaGodina sg " +
            "left join fetch dp.realizacijaPredmeta r " +
            "where n.id = :nastavnikId and sg.aktivna = true")
    List<DrziPredmet> findActiveByNastavnikId(@Param("nastavnikId") Long nastavnikId);

    @Query("select dp from DrziPredmet dp " +
            "join fetch dp.predmet p " +
            "join fetch dp.skolskaGodina sg " +
            "where sg.aktivna = true and exists (" +
            "select pp.id from ProgramPredmet pp where pp.predmet = p and pp.program.id = :programId) " +
            "order by p.id, dp.id")
    List<DrziPredmet> findActiveByProgramId(@Param("programId") Long programId);

}
