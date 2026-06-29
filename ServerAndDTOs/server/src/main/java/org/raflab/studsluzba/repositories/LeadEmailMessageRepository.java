package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.LeadEmailMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LeadEmailMessageRepository extends JpaRepository<LeadEmailMessage, Long> {
    List<LeadEmailMessage> findByLeadIdOrderByCreatedAtDesc(Long leadId);

    Page<LeadEmailMessage> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
