package org.raflab.studsluzba.repositories.enrollment;

import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequestHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StudyYearEnrollmentRequestHistoryRepository
        extends JpaRepository<StudyYearEnrollmentRequestHistory, Long> {

    List<StudyYearEnrollmentRequestHistory> findByRequestIdOrderByCreatedAtAsc(Long requestId);
}
