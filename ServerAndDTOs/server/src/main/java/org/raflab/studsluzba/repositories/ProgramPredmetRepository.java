package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ProgramPredmet;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProgramPredmetRepository extends CrudRepository<ProgramPredmet, Long> {

    @Query("select pp from ProgramPredmet pp where pp.program.id = :programId order by pp.godinaStudija, pp.semestarUGodini")
    List<ProgramPredmet> findProgramPredmeti(@Param("programId") Long programId);

    @Query("select pp from ProgramPredmet pp where pp.program.id = :programId and pp.predmet.id = :predmetId")
    Optional<ProgramPredmet> findByProgramIdAndPredmetId(@Param("programId") Long programId,
                                                         @Param("predmetId") Long predmetId);

    List<ProgramPredmet> findByPredmetId(Long predmetId);

    List<ProgramPredmet> findByProgramIdAndGodinaStudijaOrderBySemestarUGodini(Long programId, Integer godinaStudija);
}
