package org.raflab.studsluzba.controllers;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.dtos.AuthResponseDTO;
import org.raflab.studsluzba.model.dtos.LoginRequest;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.UserAccountService;
import org.raflab.studsluzba.services.PermissionService;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthControllerTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void loginSuccessStoresSecurityContextInSessionAndReturnsUser() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        CurrentUser currentUser = mock(CurrentUser.class);
        TestingAuthenticationToken auth = new TestingAuthenticationToken("admin@example.com", "ignored", "ROLE_ADMIN");
        auth.setAuthenticated(true);
        when(authenticationManager.authenticate(any())).thenReturn(auth);

        UserAccount account = new UserAccount();
        account.setId(1L);
        account.setUsername("admin@example.com");
        account.setRole(Role.ADMIN);
        account.setEnabled(true);
        when(currentUser.account()).thenReturn(account);

        AuthController controller = new AuthController(authenticationManager, currentUser, mock(UserAccountService.class), mock(PermissionService.class));
        LoginRequest request = new LoginRequest();
        request.setUsername("admin@example.com");
        request.setPassword("secret");
        MockHttpServletRequest httpRequest = new MockHttpServletRequest();

        AuthResponseDTO response = controller.login(request, httpRequest);

        assertThat(response.getUser().getUsername()).isEqualTo("admin@example.com");
        assertThat(response.getUser().getRole()).isEqualTo("ADMIN");
        Object storedContext = httpRequest.getSession(false).getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        assertThat(storedContext).isInstanceOf(SecurityContext.class);
        assertThat(((SecurityContext) storedContext).getAuthentication().getName()).isEqualTo("admin@example.com");
    }

    @Test
    void loginFailurePropagatesBadCredentials() {
        AuthenticationManager authenticationManager = mock(AuthenticationManager.class);
        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));
        AuthController controller = new AuthController(authenticationManager, mock(CurrentUser.class), mock(UserAccountService.class), mock(PermissionService.class));
        LoginRequest request = new LoginRequest();
        request.setUsername("admin@example.com");
        request.setPassword("wrong");

        assertThatThrownBy(() -> controller.login(request, new MockHttpServletRequest()))
                .isInstanceOf(BadCredentialsException.class);
    }
}
