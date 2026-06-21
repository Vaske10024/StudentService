package org.raflab.studsluzba.security;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;

class ApiErrorResponseWriterTest {

    @Test
    void writesConsistentJsonErrorBody() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        MockHttpServletResponse response = new MockHttpServletResponse();

        new ApiErrorResponseWriter(objectMapper)
                .write(response, 403, "FORBIDDEN", "Nema dozvole.", "/api/admin");

        JsonNode body = objectMapper.readTree(response.getContentAsByteArray());
        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");
        assertThat(body.get("status").asInt()).isEqualTo(403);
        assertThat(body.get("error").asText()).isEqualTo("Forbidden");
        assertThat(body.get("code").asText()).isEqualTo("FORBIDDEN");
        assertThat(body.get("message").asText()).isEqualTo("Nema dozvole.");
        assertThat(body.get("path").asText()).isEqualTo("/api/admin");
        assertThat(body.hasNonNull("timestamp")).isTrue();
    }
}
