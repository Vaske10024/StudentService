package org.raflab.studsluzba.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;
import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CookieSecurityFilter extends OncePerRequestFilter {

    @Value("${app.cookie.same-site:Lax}")
    private String sameSite;

    @Value("${server.servlet.session.cookie.secure:false}")
    private boolean secureCookie;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, new HttpServletResponseWrapper(response) {
            @Override
            public void addHeader(String name, String value) {
                super.addHeader(name, rewriteSetCookieHeader(name, value));
            }

            @Override
            public void setHeader(String name, String value) {
                super.setHeader(name, rewriteSetCookieHeader(name, value));
            }
        });
    }

    private String rewriteSetCookieHeader(String name, String value) {
        if (value == null || !"Set-Cookie".equalsIgnoreCase(name)) {
            return value;
        }
        String rewritten = value;
        if (sameSite != null && !sameSite.trim().isEmpty()
                && !rewritten.toLowerCase().contains("samesite=")) {
            rewritten = rewritten + "; SameSite=" + sameSite.trim();
        }
        if (secureCookie && !rewritten.toLowerCase().contains("secure")) {
            rewritten = rewritten + "; Secure";
        }
        return rewritten;
    }
}
