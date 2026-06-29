package org.raflab.studsluzba.repositories;

import org.raflab.studsluzba.model.LeadExportLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LeadExportLogRepository extends JpaRepository<LeadExportLog, Long> {
    Page<LeadExportLog> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
