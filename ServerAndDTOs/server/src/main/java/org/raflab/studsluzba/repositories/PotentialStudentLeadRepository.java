package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.PotentialStudentLead;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PotentialStudentLeadRepository extends JpaRepository<PotentialStudentLead, Long> {
    Page<PotentialStudentLead> findAllByOrderByCreatedAtDesc(Pageable pageable);

    List<PotentialStudentLead> findAllByOrderByCreatedAtDesc();
}
