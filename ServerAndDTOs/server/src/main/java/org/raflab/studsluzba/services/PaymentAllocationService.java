package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.finance.FinancialObligation;
import org.raflab.studsluzba.model.finance.LedgerEntry;
import org.raflab.studsluzba.model.finance.PaymentAllocation;
import org.raflab.studsluzba.repositories.finance.FinancialObligationRepository;
import org.raflab.studsluzba.repositories.finance.PaymentAllocationRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PaymentAllocationService {
    private final FinancialObligationRepository obligationRepo;
    private final PaymentAllocationRepository allocationRepo;

    public void allocateOldestFirst(LedgerEntry payment) {
        BigDecimal remaining = payment.getAmountEur().abs();
        for (FinancialObligation obligation : obligationRepo.findByStudentIndeksIdOrderByDueDateAsc(payment.getStudentIndeks().getId())) {
            BigDecimal open = obligation.getAmountEur().subtract(obligation.getAllocatedEur());
            if (open.signum() <= 0 || remaining.signum() <= 0) continue;
            BigDecimal allocated = remaining.min(open);
            PaymentAllocation allocation = new PaymentAllocation();
            allocation.setPayment(payment);
            allocation.setObligation(obligation);
            allocation.setAmountEur(allocated);
            allocationRepo.save(allocation);
            obligation.setAllocatedEur(obligation.getAllocatedEur().add(allocated));
            obligationRepo.save(obligation);
            remaining = remaining.subtract(allocated);
        }
    }
}
