package org.raflab.studsluzba.services;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequest;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

class StudyYearEnrollmentPolicyTest {

    private StudyYearEnrollmentPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new StudyYearEnrollmentPolicy();
        ReflectionTestUtils.setField(policy, "regularMinimumEctsPerYear", 48);
        ReflectionTestUtils.setField(policy, "conditionalMinimumEctsPerYear", 37);
        ReflectionTestUtils.setField(policy, "maximumTransferredEcts", 60);
    }

    @Test
    void suggestsRegularConditionalAndRenewalFromConfigurableThresholds() {
        assertThat(policy.regularThresholdForCurrentYear(2)).isEqualTo(96);
        assertThat(policy.conditionalThresholdForCurrentYear(2)).isEqualTo(74);
        assertThat(policy.suggest(2, 4, 96, 96, 74))
                .isEqualTo(StudyYearEnrollmentRequest.Type.ENROLL_NEXT_YEAR);
        assertThat(policy.suggest(2, 4, 80, 96, 74))
                .isEqualTo(StudyYearEnrollmentRequest.Type.CONDITIONAL_ENROLLMENT);
        assertThat(policy.suggest(2, 4, 60, 96, 74))
                .isEqualTo(StudyYearEnrollmentRequest.Type.RENEW_YEAR);
    }

    @Test
    void finalProgramYearCanOnlyBeRenewed() {
        assertThat(policy.suggest(4, 4, 240, 192, 148))
                .isEqualTo(StudyYearEnrollmentRequest.Type.RENEW_YEAR);
    }
}
