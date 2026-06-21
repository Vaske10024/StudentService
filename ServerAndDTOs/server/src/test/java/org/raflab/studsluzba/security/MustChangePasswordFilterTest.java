package org.raflab.studsluzba.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.servlet.FilterChain;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MustChangePasswordFilterTest {
    @AfterEach void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void temporaryPasswordBlocksBusinessEndpointButAllowsPasswordChange() throws Exception {
        UserAccountRepository repo = mock(UserAccountRepository.class);
        UserAccount account = new UserAccount();
        account.setUsername("student@example.edu");
        account.setRole(Role.STUDENT);
        account.setMustChangePassword(true);
        when(repo.findByUsername(account.getUsername())).thenReturn(Optional.of(account));
        TestingAuthenticationToken auth = new TestingAuthenticationToken(account.getUsername(), "ignored", "ROLE_STUDENT");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
        MustChangePasswordFilter filter = new MustChangePasswordFilter(repo, new ApiErrorResponseWriter(new ObjectMapper()));

        MockHttpServletResponse blocked = new MockHttpServletResponse();
        FilterChain blockedChain = mock(FilterChain.class);
        filter.doFilter(new MockHttpServletRequest("GET", "/api/me/student/dashboard"), blocked, blockedChain);
        assertThat(blocked.getStatus()).isEqualTo(403);
        assertThat(blocked.getContentType()).contains("application/json");
        assertThat(blocked.getContentAsString()).contains("MUST_CHANGE_PASSWORD");
        verifyNoInteractions(blockedChain);

        FilterChain allowedChain = mock(FilterChain.class);
        filter.doFilter(new MockHttpServletRequest("POST", "/api/auth/password"), new MockHttpServletResponse(), allowedChain);
        verify(allowedChain).doFilter(any(), any());
    }
}
