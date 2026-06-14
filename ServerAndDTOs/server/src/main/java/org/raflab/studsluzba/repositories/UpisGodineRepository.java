package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.UpisGodine;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UpisGodineRepository extends CrudRepository<UpisGodine, Long> {
    boolean existsByIndeksIdAndUpisujeGodinuAndSkolskaGodinaId(Long indeksId, int upisujeGodinu, Long skolskaGodinaId);

    @Query("select u from UpisGodine u where u.indeks.id = :indeksId order by u.datum desc, u.id desc")
    List<UpisGodine> findUpisi(@Param("indeksId") Long indeksId);
}
