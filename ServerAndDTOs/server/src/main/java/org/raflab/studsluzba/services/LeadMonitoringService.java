package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.LeadExportLog;
import org.raflab.studsluzba.model.dtos.AuditLogDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailMonitoringDTO;
import org.raflab.studsluzba.model.dtos.LeadExportLogDTO;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.repositories.LeadEmailMessageRepository;
import org.raflab.studsluzba.repositories.LeadExportLogRepository;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@PreAuthorize("hasRole('HEAD_ADMIN')")
public class LeadMonitoringService {
    private final LeadEmailMessageRepository messageRepository;
    private final LeadExportLogRepository exportRepository;
    private final AuditLogRepository auditRepository;
    private final LeadCrmService crmService;
    private final PotentialStudentLeadService leadService;

    @Transactional(readOnly = true)
    public Page<LeadEmailMonitoringDTO> emails(int page, int size) {
        return messageRepository.findAllByOrderByCreatedAtDesc(pageable(page, size))
                .map(message -> new LeadEmailMonitoringDTO(
                        crmService.toDto(message),
                        leadService.toDto(message.getLead(), true)
                ));
    }

    @Transactional(readOnly = true)
    public Page<AuditLogDTO> audits(int page, int size) {
        return auditRepository.findByActionStartingWithOrderByCreatedAtDesc("LEAD_", pageable(page, size))
                .map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Page<LeadExportLogDTO> exports(int page, int size) {
        return exportRepository.findAllByOrderByCreatedAtDesc(pageable(page, size)).map(this::toDto);
    }

    private PageRequest pageable(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.max(1, Math.min(size, 100)));
    }

    private AuditLogDTO toDto(AuditLog item) {
        return new AuditLogDTO(item.getId(), item.getActorUserId(), item.getActorUsername(),
                item.getActorRole() == null ? null : item.getActorRole().name(),
                item.getLead() == null ? null : item.getLead().getId(),
                item.getAction(), item.getDetails(), item.getOldValue(), item.getNewValue(),
                item.getIpAddress(), item.getUserAgent(), item.getCreatedAt());
    }

    private LeadExportLogDTO toDto(LeadExportLog item) {
        return new LeadExportLogDTO(item.getId(),
                item.getExportedBy() == null ? null : item.getExportedBy().getId(),
                item.getExportedBy() == null ? null : item.getExportedBy().getUsername(),
                item.getExporterRole() == null ? null : item.getExporterRole().name(),
                item.getExportType(), item.isMasked(), item.getRecordCount(), item.getFilters(),
                item.getIpAddress(), item.getUserAgent(), item.getCreatedAt());
    }
}
