package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.AuthResponseDTO;
import org.raflab.studsluzba.model.dtos.AuthUserDTO;
import org.raflab.studsluzba.model.dtos.CsrfTokenDTO;
import org.raflab.studsluzba.model.dtos.ChangePasswordRequest;
import org.raflab.studsluzba.model.dtos.LoginRequest;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.security.CurrentUser;
import org.raflab.studsluzba.services.UserAccountService;
import org.raflab.studsluzba.services.PermissionService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final CurrentUser currentUser;
    private final UserAccountService userAccountService;
    private final PermissionService permissionService;

    @GetMapping("/csrf")
    public CsrfTokenDTO csrf(CsrfToken token) {
        return new CsrfTokenDTO(token.getHeaderName(), token.getParameterName(), token.getToken());
    }

    @PostMapping("/login")
    public AuthResponseDTO login(@RequestBody @Valid LoginRequest request, HttpServletRequest httpRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(auth);
        SecurityContextHolder.setContext(context);
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, context);

        UserAccount account = currentUser.account();
        return new AuthResponseDTO(toDto(account, permissionService.currentPermissions()));
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        SecurityContextHolder.clearContext();
    }

    @GetMapping("/me")
    public AuthResponseDTO me() {
        return new AuthResponseDTO(toDto(currentUser.account(), permissionService.currentPermissions()));
    }

    @PostMapping("/password")
    public void changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        userAccountService.changeCurrentPassword(request.getCurrentPassword(), request.getNewPassword());
    }

    public static AuthUserDTO toDto(UserAccount ua) {
        return toDto(ua, java.util.Collections.emptyList());
    }

    public static AuthUserDTO toDto(UserAccount ua, java.util.List<String> permissions) {
        if (ua == null) return null;
        return new AuthUserDTO(
                ua.getId(),
                ua.getUsername(),
                ua.getRole() == null ? null : ua.getRole().name(),
                ua.isEnabled(),
                ua.getLinkedStudentPodaci() == null ? null : ua.getLinkedStudentPodaci().getId(),
                ua.getLinkedStudentIndeks() == null ? null : ua.getLinkedStudentIndeks().getId(),
                ua.getLinkedNastavnik() == null ? null : ua.getLinkedNastavnik().getId(),
                ua.isMustChangePassword(),
                permissions
        );
    }
}
