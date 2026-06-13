package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.security.*;
import org.raflab.studsluzba.repositories.security.SystemSettingRepository;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.stereotype.Service;
import java.util.List;
@Service @RequiredArgsConstructor
public class SystemSettingService {
 private final SystemSettingRepository repo;private final PermissionService permissions;private final CurrentUser currentUser;
 public List<SystemSetting> list(){permissions.require(Permission.SETTINGS_READ);return repo.findAll();}
 public SystemSetting put(String key,String value,String description){permissions.require(Permission.SETTINGS_WRITE);
  SystemSetting s=repo.findBySettingKey(key).orElseGet(SystemSetting::new);s.setSettingKey(key);s.setSettingValue(value);s.setDescription(description);s.setUpdatedByUserId(currentUser.userId());return repo.save(s);}
}
