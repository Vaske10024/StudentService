package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.security.*;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class AuditService {
 private final AuditLogRepository repo; private final PermissionService permissions;
 public Page<AuditLog> list(int page,int size){permissions.require(Permission.AUDIT_READ);return repo.findAll(PageRequest.of(page,size,Sort.by(Sort.Direction.DESC,"createdAt")));}
}
