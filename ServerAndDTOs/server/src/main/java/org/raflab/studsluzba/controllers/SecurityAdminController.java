package org.raflab.studsluzba.controllers;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.security.*;
import org.raflab.studsluzba.services.*;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController @RequiredArgsConstructor
public class SecurityAdminController {
 private final PermissionService permissionService;private final AuditService auditService;private final SystemSettingService settingService;
 @PutMapping("/api/security/users/{userId}/permissions/{permission}") public UserPermissionOverride override(@PathVariable Long userId,@PathVariable Permission permission,@RequestParam boolean allowed){return permissionService.setOverride(userId,permission,allowed);}
 @GetMapping("/api/audit") public Page<AuditLog> audit(@RequestParam(defaultValue="0")int page,@RequestParam(defaultValue="50")int size){return auditService.list(page,size);}
 @GetMapping("/api/settings") public List<SystemSetting> settings(){return settingService.list();}
 @PutMapping("/api/settings/{key}") public SystemSetting put(@PathVariable String key,@RequestParam String value,@RequestParam(required=false)String description){return settingService.put(key,value,description);}
}
