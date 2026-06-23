package org.raflab.studsluzba.services;
import org.junit.jupiter.api.Test;import org.raflab.studsluzba.model.security.*;import org.raflab.studsluzba.repositories.security.*;import org.raflab.studsluzba.security.CurrentUser;import java.util.Optional;import static org.assertj.core.api.Assertions.assertThat;import static org.mockito.Mockito.*;
class PermissionServiceTest{
 @Test void adminReceivesOperationalPermissionsByDefault(){
  CurrentUser current=mock(CurrentUser.class);RolePermissionRepository roles=mock(RolePermissionRepository.class);UserPermissionOverrideRepository overrides=mock(UserPermissionOverrideRepository.class);
  UserAccount admin=new UserAccount();admin.setId(1L);admin.setRole(Role.ADMIN);when(current.account()).thenReturn(admin);when(overrides.findByUserAccountIdAndPermission(1L,Permission.FINANCE_WRITE)).thenReturn(Optional.empty());
  PermissionService service=new PermissionService(current,roles,overrides,mock(UserAccountRepository.class));
  assertThat(service.has(Permission.FINANCE_WRITE)).isTrue();assertThat(service.has(Permission.SECURITY_ADMIN)).isTrue();
 }
 @Test void headAdminReceivesOperationalPermissionsByDefault(){
  CurrentUser current=mock(CurrentUser.class);RolePermissionRepository roles=mock(RolePermissionRepository.class);UserPermissionOverrideRepository overrides=mock(UserPermissionOverrideRepository.class);
  UserAccount admin=new UserAccount();admin.setId(2L);admin.setRole(Role.HEAD_ADMIN);when(current.account()).thenReturn(admin);when(overrides.findByUserAccountIdAndPermission(2L,Permission.LEADS_EXPORT)).thenReturn(Optional.empty());
  PermissionService service=new PermissionService(current,roles,overrides,mock(UserAccountRepository.class));
  assertThat(service.has(Permission.LEADS_EXPORT)).isTrue();assertThat(service.has(Permission.SECURITY_ADMIN)).isTrue();
 }
}
