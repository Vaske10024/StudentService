package org.raflab.studsluzba.model.enrollment;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
public class EnrollmentDecision {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private EnrollmentApplication application;
    @Column(nullable = false, length = 32) private String decision;
    private String reason;
    private Long actorUserId;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
