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

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (userAccountRepository.existsByRole(Role.ADMIN)) return;
        if (isBlank(username) || isBlank(password)) {
            log.warn("No ADMIN account exists. Set BOOTSTRAP_ADMIN_USERNAME and BOOTSTRAP_ADMIN_PASSWORD once to bootstrap the first admin.");
            return;
        }
        if (userAccountRepository.existsByUsername(username.trim())) {
            throw new IllegalStateException("Bootstrap admin username already belongs to another account.");
        }
        UserAccount admin = new UserAccount();
        admin.setUsername(username.trim());
        admin.setPasswordHash(passwordEncoder.encode(password));
        admin.setRole(Role.ADMIN);
        admin.setEnabled(true);
        admin.setMustChangePassword(false);
        userAccountRepository.save(admin);
        log.warn("Created the first ADMIN account '{}'. Remove bootstrap credentials from the environment.", admin.getUsername());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
