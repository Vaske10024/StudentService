package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.LeadEmailTemplate;
import org.raflab.studsluzba.model.dtos.LeadEmailTemplateDTO;
import org.raflab.studsluzba.model.dtos.LeadEmailTemplateRequest;
import org.raflab.studsluzba.repositories.LeadEmailTemplateRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeadEmailTemplateService {
    private final LeadEmailTemplateRepository repository;
    private final CurrentUser currentUser;

    @Transactional(readOnly = true)
    public List<LeadEmailTemplateDTO> listAvailable() {
        currentUser.requireAdmin();
        List<LeadEmailTemplate> templates = currentUser.isHeadAdmin()
                ? repository.findAllByOrderByNameAsc()
                : repository.findByActiveTrueOrderByNameAsc();
        return templates.stream().map(this::toDto).collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasRole('HEAD_ADMIN')")
    public LeadEmailTemplateDTO create(LeadEmailTemplateRequest request) {
        LeadEmailTemplate template = new LeadEmailTemplate();
        template.setCreatedBy(currentUser.account());
        apply(template, request);
        return toDto(repository.save(template));
    }

    @Transactional
    @PreAuthorize("hasRole('HEAD_ADMIN')")
    public LeadEmailTemplateDTO update(Long id, LeadEmailTemplateRequest request) {
        LeadEmailTemplate template = repository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Email template does not exist."));
        apply(template, request);
        return toDto(repository.save(template));
    }

    private void apply(LeadEmailTemplate template, LeadEmailTemplateRequest request) {
        template.setName(request.getName().trim());
        template.setSubject(request.getSubject());
        template.setBody(request.getBody());
        template.setActive(Boolean.TRUE.equals(request.getActive()));
    }

    LeadEmailTemplateDTO toDto(LeadEmailTemplate template) {
        return new LeadEmailTemplateDTO(template.getId(), template.getName(), template.getSubject(),
                template.getBody(), template.isActive(),
                template.getCreatedBy() == null ? null : template.getCreatedBy().getId(),
                template.getCreatedBy() == null ? null : template.getCreatedBy().getUsername(),
                template.getCreatedAt(), template.getUpdatedAt());
    }
}
