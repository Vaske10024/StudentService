package org.raflab.studsluzba.model.enrollment;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter @Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_enrollment_idempotency_key", columnNames = "idempotencyKey"))
public class EnrollmentApplication {
    public enum Status { DRAFT, SUBMITTED, APPROVED, REJECTED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, length = 80) private String idempotencyKey;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Status status = Status.DRAFT;
    @Column(nullable = false) private String ime;
    @Column(nullable = false) private String prezime;
    @Column(nullable = false) private String jmbg;
    @Column(nullable = false) private String email;
    @Column(nullable = false) private String username;
    @Column(nullable = false) private Long studijskiProgramId;
    @Column(nullable = false) private Integer godina;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal tuitionEur = BigDecimal.ZERO;
    @ManyToOne private StudentPodaci createdStudent;
    @ManyToOne private StudentIndeks createdIndeks;
    private String decisionReason;
    private Long decidedByUserId;
    private LocalDateTime decidedAt;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
