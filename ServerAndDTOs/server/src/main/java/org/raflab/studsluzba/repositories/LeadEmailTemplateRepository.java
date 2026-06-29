package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.LeadEmailTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadEmailTemplateRepository extends JpaRepository<LeadEmailTemplate, Long> {
    List<LeadEmailTemplate> findByActiveTrueOrderByNameAsc();

    List<LeadEmailTemplate> findAllByOrderByNameAsc();
}
