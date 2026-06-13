package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.security.*;
import org.raflab.studsluzba.repositories.security.*;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
@Service @RequiredArgsConstructor
public class PermissionService {
 private final CurrentUser currentUser;
 private final RolePermissionRepository roleRepo;
 private final UserPermissionOverrideRepository overrideRepo;
 private final UserAccountRepository userRepo;
 public boolean has(Permission permission){
  UserAccount user=currentUser.account();
  return overrideRepo.findByUserAccountIdAndPermission(user.getId(),permission).map(UserPermissionOverride::isAllowed)
    .orElseGet(()->roleRepo.existsByRoleAndPermission(user.getRole(),permission)
      || (permission==Permission.SECURITY_ADMIN && user.getRole()==Role.ADMIN));
 }
 public void require(Permission permission){if(!has(permission))throw new AccessDeniedException("Nedostaje dozvola: "+permission);}
 public UserPermissionOverride setOverride(Long userId,Permission permission,boolean allowed){
  require(Permission.SECURITY_ADMIN);
  UserPermissionOverride item=overrideRepo.findByUserAccountIdAndPermission(userId,permission).orElseGet(UserPermissionOverride::new);
  item.setUserAccount(userRepo.findById(userId).orElseThrow(()->new IllegalArgumentException("Korisnik ne postoji.")));
  item.setPermission(permission);item.setAllowed(allowed);return overrideRepo.save(item);
 }
}
