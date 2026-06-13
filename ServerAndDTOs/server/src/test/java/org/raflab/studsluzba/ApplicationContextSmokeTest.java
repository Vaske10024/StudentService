package org.raflab.studsluzba;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import java.util.Map;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:smokedb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=MySQL;DATABASE_TO_UPPER=false")
class ApplicationContextSmokeTest {
    @Autowired
    private TestRestTemplate rest;

    @Test
    void fullApplicationContextStarts() {
        ResponseEntity<Map> response = rest.getForEntity("/api/auth/csrf", Map.class);
        assertThat(response.getStatusCodeValue()).isEqualTo(200);
        assertThat(response.getHeaders().getFirst("X-Correlation-ID")).isNotBlank();
        assertThat(response.getHeaders().getFirst("X-Frame-Options")).isEqualTo("DENY");
        assertThat(response.getHeaders().getFirst("X-Content-Type-Options")).isEqualTo("nosniff");
    }
}
