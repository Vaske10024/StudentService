package org.raflab.studsluzba.repositories.academic;
import org.raflab.studsluzba.model.academic.ECTSRule;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface ECTSRuleRepository extends JpaRepository<ECTSRule,Long> {
    Optional<ECTSRule> findByProgramIdAndTargetYear(Long programId, Integer targetYear);
}
