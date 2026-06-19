package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.repositories.security.AuditLogRepository;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class GradingServiceTest {
    @Test
    void gradeIsCalculatedConsistentlyFromTotalPoints() {
        GradingService service = service();
        assertThat(service.gradeForTotalPoints(50)).isEqualTo(5);
        assertThat(service.gradeForTotalPoints(51)).isEqualTo(6);
        assertThat(service.gradeForTotalPoints(70)).isEqualTo(7);
        assertThat(service.gradeForTotalPoints(91)).isEqualTo(10);
    }

    @Test
    void pointsCannotExceedConfiguredLimitsOrTotalHundred() {
        GradingService service = service();
        assertThatThrownBy(() -> service.validateExamPoints(71)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> service.validateTotalPoints(101)).isInstanceOf(IllegalStateException.class);
    }

    private GradingService service() {
        GradingService service = new GradingService(mock(AuditLogRepository.class), mock(CurrentUser.class));
        ReflectionTestUtils.setField(service, "examMaxPoints", 70);
        ReflectionTestUtils.setField(service, "predispitMaxTotalPoints", 100);
        return service;
    }
}
