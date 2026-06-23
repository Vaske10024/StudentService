package org.raflab.studsluzba.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapInitializer implements ApplicationRunner {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap.admin.username:}")
    private String username;

    @Value("${app.bootstrap.admin.password:}")
    private String password;

    @Value("${app.bootstrap.admin.role:HEAD_ADMIN}")
    private String role;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userAccountRepository.existsByRoleIn(Arrays.asList(Role.ADMIN, Role.HEAD_ADMIN))) return;
        if (isBlank(username) || isBlank(password)) {
            log.warn("No ADMIN or HEAD_ADMIN account exists. Set BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_PASSWORD once to bootstrap the first admin.");
            return;
        }
        if (userAccountRepository.existsByUsername(username.trim())) {
            throw new IllegalStateException("Bootstrap admin username already belongs to another account.");
        }
        Role bootstrapRole = bootstrapRole();
        UserAccount admin = new UserAccount();
        admin.setUsername(username.trim());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(bootstrapRole);
        admin.setEnabled(true);
        admin.setMustChangePassword(false);
        userAccountRepository.save(admin);
        log.warn("Created the first {} account '{}'. Remove bootstrap credentials from the environment.", bootstrapRole, admin.getUsername());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private Role bootstrapRole() {
        String configuredRole = isBlank(role) ? Role.HEAD_ADMIN.name() : role.trim().toUpperCase();
        Role configured;
        try {
            configured = Role.valueOf(configuredRole);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("Bootstrap admin role must be ADMIN or HEAD_ADMIN.");
        }
        if (configured != Role.ADMIN && configured != Role.HEAD_ADMIN) {
            throw new IllegalStateException("Bootstrap admin role must be ADMIN or HEAD_ADMIN.");
        }
        return configured;
    }
}
