package org.raflab.studsluzba.repositories.security;
import org.raflab.studsluzba.model.security.SystemSetting;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface SystemSettingRepository extends JpaRepository<SystemSetting,Long>{
 Optional<SystemSetting> findBySettingKey(String key);
}
