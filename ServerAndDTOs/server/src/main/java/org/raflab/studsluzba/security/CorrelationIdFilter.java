package org.raflab.studsluzba.security;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.UUID;
@Component @Order(Ordered.HIGHEST_PRECEDENCE)
public class CorrelationIdFilter extends OncePerRequestFilter {
 protected void doFilterInternal(HttpServletRequest req,HttpServletResponse res,FilterChain chain)throws ServletException,IOException{
  String id=req.getHeader("X-Correlation-ID");if(id==null||id.isBlank())id=UUID.randomUUID().toString();
  res.setHeader("X-Correlation-ID",id);chain.doFilter(req,res);
 }
}
