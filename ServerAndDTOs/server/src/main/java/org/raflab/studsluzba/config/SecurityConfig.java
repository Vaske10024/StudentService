package org.raflab.studsluzba.config;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.security.UserAccountDetailsService;
import org.raflab.studsluzba.security.MustChangePasswordFilter;
import org.raflab.studsluzba.security.ApiErrorResponseWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserAccountDetailsService userDetailsService;
    private final MustChangePasswordFilter mustChangePasswordFilter;
    private final ApiErrorResponseWriter apiErrorResponseWriter;

    @Value("${app.cors.allowed-origins:http://localhost:5173}")
    private String allowedOrigins;

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split("\\s*,\\s*")));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-XSRF-TOKEN", "X-Requested-With"));
        config.setExposedHeaders(Arrays.asList("X-XSRF-TOKEN", "X-Correlation-ID"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        CookieCsrfTokenRepository csrfTokenRepository = CookieCsrfTokenRepository.withHttpOnlyFalse();
        csrfTokenRepository.setCookiePath("/");

        http
            .cors().and()
            .csrf().csrfTokenRepository(csrfTokenRepository).and()
            .authorizeRequests()
                .antMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .antMatchers(HttpMethod.GET, "/api/auth/csrf").permitAll()
                .antMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                .antMatchers("/api/auth/logout", "/api/auth/me", "/api/auth/password", "/api/me/**").authenticated()

                .antMatchers(HttpMethod.POST, "/api/student/add", "/api/student/saveindeks", "/api/student/saveindeks/provision").hasRole("ADMIN")
                .antMatchers("/api/student/all", "/api/student/svi", "/api/student/search", "/api/student/global-search", "/api/student/fastsearch", "/api/student/emailsearch", "/api/student/podaci/**", "/api/student/indeks/**", "/api/student/indeksi/**").hasRole("ADMIN")
                .antMatchers("/api/student/profile/**", "/api/student/webprofile/**").authenticated()
                .antMatchers("/api/student/query/**").hasRole("ADMIN")

                .antMatchers(HttpMethod.POST, "/api/nastavnik/**").hasRole("ADMIN")
                .antMatchers("/api/nastavnik/zvanje/**").hasRole("ADMIN")
                .antMatchers("/api/nastavnik/**").hasRole("ADMIN")

                .antMatchers("/api/predmet/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/studprogram/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/studprogram/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/studprogram/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/program/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/program/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/program/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/program/**").hasRole("ADMIN")
                .antMatchers("/api/program/**", "/api/studprogram/**", "/api/predmet/**").authenticated()

                .antMatchers(HttpMethod.POST, "/api/sg/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/sg/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/sg/**").hasRole("ADMIN")
                .antMatchers("/api/sg/**").authenticated()

                .antMatchers("/api/studij/**").hasRole("ADMIN")
                .antMatchers("/api/enrollment/year-requests/me", "/api/enrollment/year-requests/me/**").hasRole("STUDENT")
                .antMatchers("/api/enrollment/year-requests/admin", "/api/enrollment/year-requests/admin/**").hasRole("ADMIN")
                .antMatchers("/api/enrollment/applications", "/api/enrollment/applications/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/uplate/dodaj").hasRole("ADMIN")
                .antMatchers("/api/uplate/list", "/api/uplate/saldo").authenticated()
                .antMatchers(HttpMethod.POST, "/api/finance/**").hasRole("ADMIN")
                .antMatchers("/api/finance/**").authenticated()

                .antMatchers("/api/ispit/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/ispit/priznaj").hasRole("ADMIN")
                .antMatchers("/api/ispit/**").authenticated()

                .antMatchers("/api/predispit/admin/**").hasAnyRole("ADMIN", "PROFESSOR")
                .antMatchers("/api/predispit/**").authenticated()
                .antMatchers("/api/realizacija/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/drzi/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/drzi/**").hasRole("ADMIN")
                .antMatchers("/api/drzi/**").hasAnyRole("ADMIN", "PROFESSOR")
                .antMatchers(HttpMethod.POST, "/api/slusa/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/slusa/**").hasRole("ADMIN")
                .antMatchers("/api/slusa/**").authenticated()

                .antMatchers(HttpMethod.POST, "/api/rok/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PUT, "/api/rok/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.PATCH, "/api/rok/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.DELETE, "/api/rok/**").hasRole("ADMIN")
                .antMatchers("/api/rok/**").authenticated()

                .antMatchers(HttpMethod.POST, "/api/srednja/**", "/api/visoka/**").hasRole("ADMIN")
                .antMatchers("/api/srednja/**", "/api/visoka/**").hasRole("ADMIN")
                .antMatchers("/actuator/**").hasRole("ADMIN")
                .antMatchers("/api/security/**", "/api/audit/**", "/api/settings/**", "/api/academic/**", "/api/reports/**").hasRole("ADMIN")
                .antMatchers("/api/requests/admin", "/api/requests/admin/**").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/requests/*/approve", "/api/requests/*/reject").hasRole("ADMIN")
                .antMatchers("/api/requests", "/api/requests/**").authenticated()
                .antMatchers("/api/student-lifecycle/requests/*/approve", "/api/student-lifecycle/requests/*/reject").hasRole("ADMIN")
                .antMatchers(HttpMethod.POST, "/api/student-lifecycle/*/status").hasRole("ADMIN")
                .antMatchers("/api/student-lifecycle", "/api/student-lifecycle/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/schedule/**").hasRole("ADMIN")
                .antMatchers("/api/schedule/**").authenticated()
                .antMatchers("/api/notifications", "/api/notifications/**").authenticated()
                .anyRequest().authenticated()
            .and()
            .exceptionHandling()
                .authenticationEntryPoint((request, response, exception) ->
                        apiErrorResponseWriter.write(response, 401, "UNAUTHENTICATED",
                                "Korisnik nije prijavljen.", request.getRequestURI()))
                .accessDeniedHandler((request, response, exception) ->
                        apiErrorResponseWriter.write(response, 403, "FORBIDDEN",
                                "Korisnik nema potrebnu dozvolu.", request.getRequestURI()))
            .and()
            .formLogin().disable()
            .httpBasic().disable()
            .logout().disable()
            .sessionManagement().sessionFixation().migrateSession();
        http.addFilterAfter(mustChangePasswordFilter, UsernamePasswordAuthenticationFilter.class);
    }
}
