package org.raflab.studsluzba.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DevLegacyDataNormalizer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.update(
                "UPDATE student_indeks "
                        + "SET status = CASE WHEN aktivan = 1 THEN 'AKTIVAN' ELSE 'NEAKTIVAN' END "
                        + "WHERE status IS NULL OR TRIM(status) = ''");
    }
}
