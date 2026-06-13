package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.model.StudentIndeks;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface SlusaPredmetRepository extends CrudRepository<SlusaPredmet, Long> {

    boolean existsByStudentIndeksIdAndDrziPredmetId(Long studentIndeksId, Long drziPredmetId);
    boolean existsByStudentIndeksIdAndRealizacijaPredmetaId(Long studentIndeksId, Long realizacijaPredmetaId);

    @Query("select distinct sp.studentIndeks from SlusaPredmet sp " +
            "where sp.realizacijaPredmeta.id = (select dp.realizacijaPredmeta.id from DrziPredmet dp where dp.id = :drziPredmetId)")
    List<StudentIndeks> getStudentiSlusaPredmetZaDrziPredmet(@Param("drziPredmetId") Long drziPredmetId);

    @Query("select case when count(sp)>0 then true else false end " +
            "from SlusaPredmet sp " +
            "where sp.studentIndeks.id = :studentIndeksId " +
            "and sp.realizacijaPredmeta.programPredmet.predmet.id = :predmetId " +
            "and sp.skolskaGodina.aktivna = true")
    boolean existsStudentSlusaPredmetAktivna(@Param("studentIndeksId") Long studentIndeksId,
                                             @Param("predmetId") Long predmetId);

    // ISPRAVKA: stvarno filtrira aktivnu školsku godinu
    @EntityGraph(attributePaths = {"realizacijaPredmeta", "realizacijaPredmeta.programPredmet",
            "realizacijaPredmeta.programPredmet.predmet", "realizacijaPredmeta.programPredmet.program",
            "realizacijaPredmeta.angazovanja", "realizacijaPredmeta.angazovanja.nastavnik",
            "drziPredmet", "drziPredmet.nastavnik", "skolskaGodina"})
    @Query("select sp from SlusaPredmet sp " +
            "where sp.studentIndeks.id = :indeksId " +
            "and sp.skolskaGodina.aktivna = true")
    List<SlusaPredmet> getSlusaPredmetForIndeksAktivnaGodina(@Param("indeksId") Long indeksId);

    @Query("select sp.studentIndeks " +
            "from SlusaPredmet sp " +
            "join sp.realizacijaPredmeta r join r.angazovanja dp " +
            "where r.programPredmet.predmet.id = :idPredmeta " +
            "and dp.nastavnik.id = :idNastavnika")
    List<StudentIndeks> getStudentiSlusaPredmetAktivnaGodina(@Param("idPredmeta") Long idPredmeta,
                                                             @Param("idNastavnika") Long idNastavnika);
}
