package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IspitniRokRepository extends CrudRepository<IspitniRok, Long> { }