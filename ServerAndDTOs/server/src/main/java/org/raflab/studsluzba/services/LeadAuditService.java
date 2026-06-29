package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.PotentialStudentLead;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LeadAuditService {
    private final AuditLogRepository auditRepository;
    private final UserAccountRepository userRepository;

    public AuditLog log(PotentialStudentLead lead, String action, String description,
                        String oldValue, String newValue, String ipAddress, String userAgent) {
        AuditLog entry = new AuditLog();
        UserAccount actor = actor();
        if (actor != null) {
            entry.setActorUserId(actor.getId());
            entry.setActorUsername(actor.getUsername());
            entry.setActorRole(actor.getRole());
        }
        entry.setLead(lead);
        entry.setAction(action);
        entry.setDetails(limit(description, 2000));
        entry.setOldValue(limit(oldValue, 1000));
        entry.setNewValue(limit(newValue, 1000));
        entry.setIpAddress(limit(ipAddress, 64));
        entry.setUserAgent(limit(userAgent, 255));
        return auditRepository.save(entry);
    }

    public void unauthorized(String path, String ipAddress, String userAgent) {
        log(null, "LEAD_UNAUTHORIZED_ACCESS", "Restricted lead endpoint denied: " + path,
                null, null, ipAddress, userAgent);
    }

    private UserAccount actor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        return userRepository.findByUsername(authentication.getName()).orElse(null);
    }

    private String limit(String value, int max) {
        return value == null || value.length() <= max ? value : value.substring(0, max);
    }
}
