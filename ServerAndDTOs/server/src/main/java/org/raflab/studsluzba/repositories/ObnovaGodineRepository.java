package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ObnovaGodine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ObnovaGodineRepository extends CrudRepository<ObnovaGodine, Long> {

    @Query("select o from ObnovaGodine o where o.indeks.id = :indeksId order by o.datum desc")
    List<ObnovaGodine> findObnove(@Param("indeksId") Long indeksId);
}
