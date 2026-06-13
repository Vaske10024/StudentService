package org.raflab.studsluzba.repositories.security;

import org.raflab.studsluzba.model.security.AuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
