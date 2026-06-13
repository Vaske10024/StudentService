package org.raflab.studsluzba.security;
import org.springframework.core.Ordered;import org.springframework.core.annotation.Order;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;import javax.servlet.http.*;import java.io.IOException;
@Component @Order(Ordered.HIGHEST_PRECEDENCE+1)
public class SecurityHeadersFilter extends OncePerRequestFilter{
 protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain c)throws ServletException,IOException{
  s.setHeader("X-Content-Type-Options","nosniff");s.setHeader("X-Frame-Options","DENY");s.setHeader("Referrer-Policy","no-referrer");
  s.setHeader("Content-Security-Policy","default-src 'self'");c.doFilter(r,s);
 }
}
