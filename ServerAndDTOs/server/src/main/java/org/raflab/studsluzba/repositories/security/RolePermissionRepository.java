package org.raflab.studsluzba.repositories.security;
import org.raflab.studsluzba.model.security.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface RolePermissionRepository extends JpaRepository<RolePermission,Long> {
 boolean existsByRoleAndPermission(Role role, Permission permission);
}
