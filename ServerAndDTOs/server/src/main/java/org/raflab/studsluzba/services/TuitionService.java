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
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class TuitionService {
    private final TuitionPlanRepository planRepo;
    private final FinancialObligationRepository obligationRepo;
    private final FinanceService financeService;

    public TuitionPlan createInitialPlan(StudentIndeks indeks, FinancingType type, BigDecimal totalEur) {
        TuitionPlan plan = new TuitionPlan();
        plan.setStudentIndeks(indeks);
        plan.setFinancingType(type);
        plan.setTotalEur(totalEur == null ? BigDecimal.ZERO : totalEur);
        TuitionPlan saved = planRepo.save(plan);
        if (saved.getTotalEur().signum() > 0) {
            financeService.createObligation(indeks.getId(), saved.getTotalEur(), LocalDate.now(), "TUITION");
            saved.setLocked(true);
            return planRepo.save(saved);
        }
        return saved;
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
