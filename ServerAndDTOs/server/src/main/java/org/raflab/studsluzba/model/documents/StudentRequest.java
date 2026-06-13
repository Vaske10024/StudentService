package org.raflab.studsluzba.model.documents;
import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import javax.persistence.*;
import java.time.*;
@Entity @Getter @Setter
public class StudentRequest {
    public enum Status { SUBMITTED, IN_REVIEW, APPROVED, REJECTED, CANCELLED }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private StudentIndeks studentIndeks;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 64) private RequestType type;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Status status = Status.SUBMITTED;
    @Column(nullable = false, length = 2000) private String reason;
    private LocalDate requestedFrom;
    private LocalDate requestedTo;
    private Long submittedByUserId;
    private Long decidedByUserId;
    private String decisionNote;
    @Column(nullable = false) private LocalDateTime createdAt;
    private LocalDateTime decidedAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
