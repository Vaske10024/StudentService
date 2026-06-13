package org.raflab.studsluzba.repositories;


import org.raflab.studsluzba.model.VisokaSkola;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisokaSkolaRepository extends JpaRepository<VisokaSkola, Long> {
    VisokaSkola findByNaziv(String naziv);
}
