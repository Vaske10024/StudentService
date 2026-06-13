package org.raflab.studsluzba.repositories.security;
import org.raflab.studsluzba.model.security.*;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface UserPermissionOverrideRepository extends JpaRepository<UserPermissionOverride,Long> {
 Optional<UserPermissionOverride> findByUserAccountIdAndPermission(Long userId,Permission permission);
}
