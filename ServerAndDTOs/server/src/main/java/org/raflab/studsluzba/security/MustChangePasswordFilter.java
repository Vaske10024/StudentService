package org.raflab.studsluzba.security;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {
    private final UserAccountRepository userAccountRepository;
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)
                && !isAllowed(request.getRequestURI())) {
            boolean blocked = userAccountRepository.findByUsername(authentication.getName())
                    .map(account -> account.getRole() != Role.ADMIN && account.isMustChangePassword())
                    .orElse(false);
            if (blocked) {
                apiErrorResponseWriter.write(response, HttpServletResponse.SC_FORBIDDEN, "MUST_CHANGE_PASSWORD",
                        "Privremena lozinka mora biti promenjena pre koriscenja sistema.",
                        request.getRequestURI());
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    private boolean isAllowed(String uri) {
        return "/api/auth/csrf".equals(uri)
                || "/api/auth/login".equals(uri)
                || "/api/auth/me".equals(uri)
                || "/api/auth/password".equals(uri)
                || "/api/auth/logout".equals(uri);
    }
}
