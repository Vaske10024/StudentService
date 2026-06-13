package org.raflab.studsluzba.security;
import org.springframework.core.Ordered;import org.springframework.core.annotation.Order;import org.springframework.stereotype.Component;import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;import javax.servlet.http.*;import java.io.IOException;import java.time.*;import java.util.concurrent.*;
@Component @Order(Ordered.HIGHEST_PRECEDENCE+2)
public class RateLimitFilter extends OncePerRequestFilter{
 private static class Bucket{long minute;int count;} private final ConcurrentMap<String,Bucket>buckets=new ConcurrentHashMap<>();
 protected void doFilterInternal(HttpServletRequest r,HttpServletResponse s,FilterChain c)throws ServletException,IOException{
  String path=r.getRequestURI();int limit=path.equals("/api/auth/login")?10:(path.contains("/upload")||path.contains("/export")||path.contains("/search")?60:300);
  long minute=Instant.now().getEpochSecond()/60;String key=r.getRemoteAddr()+":"+path;Bucket b=buckets.computeIfAbsent(key,k->new Bucket());
  synchronized(b){if(b.minute!=minute){b.minute=minute;b.count=0;}if(++b.count>limit){s.sendError(429,"Rate limit exceeded");return;}}
  c.doFilter(r,s);
 }
}
