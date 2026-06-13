package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RealizacijaPredmetaRepository extends JpaRepository<RealizacijaPredmeta, Long> {

    Optional<RealizacijaPredmeta> findByProgramPredmetIdAndSkolskaGodinaId(Long programPredmetId, Long skolskaGodinaId);

    @EntityGraph(attributePaths = {"programPredmet", "programPredmet.program", "programPredmet.predmet", "skolskaGodina"})
    List<RealizacijaPredmeta> findAllBySkolskaGodinaIdOrderById(Long skolskaGodinaId);

    @Query("select r from RealizacijaPredmeta r " +
            "join fetch r.programPredmet pp join fetch pp.predmet p join fetch pp.program prog " +
            "join fetch r.skolskaGodina sg " +
            "where prog.id = :programId and pp.godinaStudija = :godinaStudija and sg.id = :sgId " +
            "order by pp.semestarUGodini, p.naziv")
    List<RealizacijaPredmeta> findForEnrollment(@Param("programId") Long programId,
                                                @Param("godinaStudija") int godinaStudija,
                                                @Param("sgId") Long skolskaGodinaId);
}
