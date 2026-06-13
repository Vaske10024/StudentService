package org.raflab.studsluzba.repositories.enrollment;
import org.raflab.studsluzba.model.enrollment.EnrollmentApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface EnrollmentApplicationRepository extends JpaRepository<EnrollmentApplication, Long> {
    Optional<EnrollmentApplication> findByIdempotencyKey(String key);
}
