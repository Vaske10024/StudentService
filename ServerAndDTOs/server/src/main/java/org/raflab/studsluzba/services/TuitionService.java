package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.finance.FinancingType;
import org.raflab.studsluzba.model.finance.TuitionPlan;
import org.raflab.studsluzba.repositories.finance.FinancialObligationRepository;
import org.raflab.studsluzba.repositories.finance.TuitionPlanRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class TuitionService {
    private final TuitionPlanRepository planRepo;
    private final FinancialObligationRepository obligationRepo;

    public TuitionPlan createInitialPlan(StudentIndeks indeks, FinancingType type, BigDecimal totalEur) {
        TuitionPlan plan = new TuitionPlan();
        plan.setStudentIndeks(indeks);
        plan.setFinancingType(type);
        plan.setTotalEur(totalEur == null ? BigDecimal.ZERO : totalEur);
        return planRepo.save(plan);
    }

    public TuitionPlan updateTotal(Long planId, BigDecimal totalEur) {
        TuitionPlan plan = planRepo.findById(planId).orElseThrow(() -> ApiException.notFound("Plan ne postoji: " + planId));
        if (plan.isLocked() || obligationRepo.existsByTuitionPlanId(planId)) {
            throw ApiException.conflict("TUITION_PLAN_IMMUTABLE", "Plan sa aktivnim zaduzenjima se ne moze menjati.");
        }
        plan.setTotalEur(totalEur);
        return planRepo.save(plan);
    }
}
