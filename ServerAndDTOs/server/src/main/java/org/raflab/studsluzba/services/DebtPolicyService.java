package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.SaldoResponse;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class DebtPolicyService {

    private final UplataService uplataService;

    @Value("${finance.exam-registration.max-debt-eur:0.00}")
    private BigDecimal maxExamRegistrationDebtEur;

    public void assertExamRegistrationAllowed(Long studentIndeksId) {
        SaldoResponse saldo = uplataService.saldo(studentIndeksId);
        BigDecimal debt = saldo.getPreostaloEur() == null ? BigDecimal.ZERO : saldo.getPreostaloEur();
        BigDecimal limit = maxExamRegistrationDebtEur == null ? BigDecimal.ZERO : maxExamRegistrationDebtEur;
        if (debt.compareTo(limit) > 0) {
            throw ApiException.conflict("DEBT_BLOCKS_EXAM_REGISTRATION",
                    "Student ima dug veci od dozvoljenog limita za prijavu ispita.");
        }
    }
}
