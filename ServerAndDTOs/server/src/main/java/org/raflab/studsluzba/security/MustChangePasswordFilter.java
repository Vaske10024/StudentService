package org.raflab.studsluzba.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.http.MediaType;
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
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class MustChangePasswordFilter extends OncePerRequestFilter {
    private final UserAccountRepository userAccountRepository;
    private final ObjectMapper objectMapper;

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
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                Map<String, Object> body = new LinkedHashMap<>();
                body.put("timestamp", Instant.now().toString());
                body.put("status", HttpServletResponse.SC_FORBIDDEN);
                body.put("error", "Forbidden");
                body.put("code", "MUST_CHANGE_PASSWORD");
                body.put("message", "Privremena lozinka mora biti promenjena pre korišćenja sistema.");
                body.put("path", request.getRequestURI());
                objectMapper.writeValue(response.getOutputStream(), body);
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
