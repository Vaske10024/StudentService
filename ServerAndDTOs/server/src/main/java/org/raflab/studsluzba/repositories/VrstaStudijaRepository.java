package org.raflab.studsluzba.repositories;



import org.raflab.studsluzba.model.VrstaStudija;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VrstaStudijaRepository extends CrudRepository<VrstaStudija, Long> {
    boolean existsBySkracenicaIgnoreCase(String skracenica);
}
