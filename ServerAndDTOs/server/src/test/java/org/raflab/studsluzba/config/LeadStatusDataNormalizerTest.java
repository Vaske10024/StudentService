package org.raflab.studsluzba.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LeadStatusDataNormalizerTest {

    @Test
    void normalizesBlankLowercaseAndInvalidLeadStatuses() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource());
        jdbcTemplate.execute("CREATE TABLE potential_student_lead (id BIGINT AUTO_INCREMENT PRIMARY KEY, status VARCHAR(32))");
        jdbcTemplate.update("INSERT INTO potential_student_lead(status) VALUES (?)", "");
        jdbcTemplate.update("INSERT INTO potential_student_lead(status) VALUES (?)", "contacted");
        jdbcTemplate.update("INSERT INTO potential_student_lead(status) VALUES (?)", "  interested  ");
        jdbcTemplate.update("INSERT INTO potential_student_lead(status) VALUES (?)", "UNKNOWN");
        jdbcTemplate.update("INSERT INTO potential_student_lead(status) VALUES (?)", "NEW");

        new LeadStatusDataNormalizer(jdbcTemplate).run(null);

        List<String> statuses = jdbcTemplate.queryForList(
                "SELECT status FROM potential_student_lead ORDER BY id", String.class);
        assertThat(statuses).containsExactly("NEW", "CONTACTED", "INTERESTED", "NEW", "NEW");
    }

    private DriverManagerDataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.h2.Driver");
        dataSource.setUrl("jdbc:h2:mem:lead-status-normalizer;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false");
        dataSource.setUsername("sa");
        dataSource.setPassword("");
        return dataSource;
    }
}
