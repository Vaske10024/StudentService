package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.StudsluzbaServerApp;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = StudsluzbaServerApp.class)
@ActiveProfiles("test")
class LeadMonitoringSecurityTest {
    @Autowired
    private LeadMonitoringService service;

    @Test
    @WithMockUser(username = "admin@example.test", roles = "ADMIN")
    void adminCannotAccessHeadAdminMonitoringData() {
        assertThatThrownBy(() -> service.emails(0, 20))
                .isInstanceOf(AccessDeniedException.class);
    }
}
