package org.raflab.studsluzba.repositories.finance;
import org.raflab.studsluzba.model.finance.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    List<LedgerEntry> findByStudentIndeksIdOrderByCreatedAtAsc(Long indeksId);
}
