package org.raflab.studsluzba.repositories.finance;
import org.raflab.studsluzba.model.finance.TuitionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface TuitionPlanRepository extends JpaRepository<TuitionPlan, Long> {
    List<TuitionPlan> findByStudentIndeksId(Long indeksId);
}
