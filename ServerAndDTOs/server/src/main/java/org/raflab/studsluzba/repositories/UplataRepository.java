package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.Uplata;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface UplataRepository extends CrudRepository<Uplata, Long> {

    @Query("select coalesce(sum(u.iznosRsd),0) from Uplata u where u.indeks.id = :indeksId")
    BigDecimal sumaUplataRsd(@Param("indeksId") Long indeksId);

    List<Uplata> findByIndeksId(Long indeksId);
}
