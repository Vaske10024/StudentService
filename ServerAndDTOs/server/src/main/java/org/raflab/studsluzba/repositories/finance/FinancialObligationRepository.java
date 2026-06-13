package org.raflab.studsluzba.repositories.finance;
import org.raflab.studsluzba.model.finance.FinancialObligation;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface FinancialObligationRepository extends JpaRepository<FinancialObligation, Long> {
    List<FinancialObligation> findByStudentIndeksIdOrderByDueDateAsc(Long indeksId);
    boolean existsByTuitionPlanId(Long planId);
}
