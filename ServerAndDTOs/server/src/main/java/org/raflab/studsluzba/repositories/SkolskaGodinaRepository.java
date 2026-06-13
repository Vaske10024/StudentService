package org.raflab.studsluzba.repositories;


import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;

@Repository
public interface SkolskaGodinaRepository extends CrudRepository<SkolskaGodina, Long> {
    SkolskaGodina findFirstByAktivnaTrue();

    List<SkolskaGodina> findAllByAktivnaTrue();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select sg from SkolskaGodina sg")
    List<SkolskaGodina> lockAllForActivation();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update SkolskaGodina sg set sg.aktivna = false where sg.aktivna = true")
    int deactivateAllActive();
}
