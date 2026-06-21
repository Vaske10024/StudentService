package org.raflab.studsluzba.security;

import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE + 2)
public class RateLimitFilter extends OncePerRequestFilter {
    private static class Bucket {
        long minute;
        int count;
    }

    private final ConcurrentMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        int limit = path.equals("/api/auth/login")
                ? 10
                : (path.contains("/upload") || path.contains("/export") || path.contains("/search") ? 60 : 300);
        long minute = Instant.now().getEpochSecond() / 60;
        String key = request.getRemoteAddr() + ":" + path;
        Bucket bucket = buckets.computeIfAbsent(key, ignored -> new Bucket());
        synchronized (bucket) {
            if (bucket.minute != minute) {
                bucket.minute = minute;
                bucket.count = 0;
            }
            if (++bucket.count > limit) {
                apiErrorResponseWriter.write(response, 429, "RATE_LIMIT_EXCEEDED",
                        "Previse zahteva. Pokusajte ponovo kasnije.", path);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
