package org.raflab.studsluzba.config;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AdminBootstrapInitializerTest {
    @Test
    void createsOnlyFirstHeadAdminFromConfiguredCredentials() throws Exception {
        UserAccountRepository repo = mock(UserAccountRepository.class);
        when(repo.existsByRoleIn(anyCollection())).thenReturn(false);
        when(repo.existsByUsername("first-admin@example.edu")).thenReturn(false);
        when(repo.save(any(UserAccount.class))).thenAnswer(i -> i.getArgument(0));
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        AdminBootstrapInitializer initializer = new AdminBootstrapInitializer(repo, encoder);
        ReflectionTestUtils.setField(initializer, "username", "first-admin@example.edu");
        ReflectionTestUtils.setField(initializer, "password", "strong-bootstrap-password");

        initializer.run(new DefaultApplicationArguments(new String[0]));

        verify(repo).save(argThat(account -> account.getRole() == Role.HEAD_ADMIN
                && encoder.matches("strong-bootstrap-password", account.getPasswordHash())
                && !account.isMustChangePassword()));
    }

    @Test
    void existingAdminDisablesBootstrap() throws Exception {
        UserAccountRepository repo = mock(UserAccountRepository.class);
        when(repo.existsByRoleIn(anyCollection())).thenReturn(true);
        AdminBootstrapInitializer initializer = new AdminBootstrapInitializer(repo, new BCryptPasswordEncoder());
        initializer.run(new DefaultApplicationArguments(new String[0]));
        verify(repo, never()).save(any());
    }
}
