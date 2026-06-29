package org.raflab.studsluzba.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class LeadStatusDataNormalizer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        int blankStatuses = jdbcTemplate.update(
                "UPDATE potential_student_lead "
                        + "SET status = 'NEW' "
                        + "WHERE status IS NULL OR TRIM(status) = ''");

        int normalizedStatuses = jdbcTemplate.update(
                "UPDATE potential_student_lead "
                        + "SET status = UPPER(TRIM(status)) "
                        + "WHERE status IS NOT NULL AND TRIM(status) <> ''");

        int invalidStatuses = jdbcTemplate.update(
                "UPDATE potential_student_lead "
                        + "SET status = 'NEW' "
                        + "WHERE status NOT IN ('NEW', 'CONTACTED', 'INTERESTED', 'NOT_INTERESTED', 'ENROLLED', 'INVALID')");

        int changedRows = blankStatuses + normalizedStatuses + invalidStatuses;
        if (changedRows > 0) {
            log.warn("Normalized {} potential_student_lead status value(s).", changedRows);
        }
    }
}
