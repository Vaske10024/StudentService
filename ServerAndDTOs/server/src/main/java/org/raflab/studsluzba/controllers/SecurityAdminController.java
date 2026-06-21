package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.AuditLogDTO;
import org.raflab.studsluzba.model.dtos.SystemSettingDTO;
import org.raflab.studsluzba.model.dtos.UserPermissionOverrideDTO;
import org.raflab.studsluzba.model.security.AuditLog;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.model.security.SystemSetting;
import org.raflab.studsluzba.model.security.UserPermissionOverride;
import org.raflab.studsluzba.services.AuditService;
import org.raflab.studsluzba.services.PermissionService;
import org.raflab.studsluzba.services.SystemSettingService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class SecurityAdminController {
    private final PermissionService permissionService;
    private final AuditService auditService;
    private final SystemSettingService settingService;

    @PutMapping("/api/security/users/{userId}/permissions/{permission}")
    public UserPermissionOverrideDTO override(@PathVariable Long userId,
                                              @PathVariable Permission permission,
                                              @RequestParam boolean allowed) {
        return toDto(permissionService.setOverride(userId, permission, allowed));
    }

    @GetMapping("/api/audit")
    public Page<AuditLogDTO> audit(@RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "50") int size) {
        return auditService.list(page, size).map(this::toDto);
    }

    @GetMapping("/api/settings")
    public List<SystemSettingDTO> settings() {
        return settingService.list().stream().map(this::toDto).collect(Collectors.toList());
    }

    @PutMapping("/api/settings/{key}")
    public SystemSettingDTO put(@PathVariable String key,
                                @RequestParam String value,
                                @RequestParam(required = false) String description) {
        return toDto(settingService.put(key, value, description));
    }

    private UserPermissionOverrideDTO toDto(UserPermissionOverride item) {
        return new UserPermissionOverrideDTO(
                item.getId(),
                item.getUserAccount() == null ? null : item.getUserAccount().getId(),
                item.getUserAccount() == null ? null : item.getUserAccount().getUsername(),
                item.getPermission() == null ? null : item.getPermission().name(),
                item.isAllowed()
        );
    }

    private AuditLogDTO toDto(AuditLog item) {
        return new AuditLogDTO(item.getId(), item.getActorUserId(), item.getAction(),
                item.getDetails(), item.getCreatedAt());
    }

    private SystemSettingDTO toDto(SystemSetting item) {
        return new SystemSettingDTO(item.getId(), item.getSettingKey(), item.getSettingValue(),
                item.getDescription(), item.getUpdatedByUserId(), item.getUpdatedAt());
    }
}
