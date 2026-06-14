package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.dtos.FinanceBalanceDTO;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DebtPolicyServiceTest {
    @Test
    void examRegistrationUsesLedgerDebt() {
        FinanceService finance = mock(FinanceService.class);
        DebtPolicyService policy = new DebtPolicyService(finance);
        ReflectionTestUtils.setField(policy, "maxExamRegistrationDebtEur", BigDecimal.ZERO);
        when(finance.balance(7L)).thenReturn(new FinanceBalanceDTO(new BigDecimal("50"), new BigDecimal("50"), BigDecimal.ZERO));

        assertThatThrownBy(() -> policy.assertExamRegistrationAllowed(7L))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("DEBT_BLOCKS_EXAM_REGISTRATION");
    }
}
