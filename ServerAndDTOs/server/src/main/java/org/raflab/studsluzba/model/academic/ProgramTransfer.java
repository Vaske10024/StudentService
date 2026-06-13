package org.raflab.studsluzba.model.academic;
import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
public class ProgramTransfer {
    public enum Status { REQUESTED, APPROVED, REJECTED }
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private StudentIndeks studentIndeks;
    @ManyToOne(optional=false) private StudijskiProgram fromProgram;
    @ManyToOne(optional=false) private StudijskiProgram toProgram;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=32) private Status status=Status.REQUESTED;
    @Column(nullable=false,length=1000) private String reason;
    private Long decidedByUserId;
    private LocalDateTime decidedAt;
    @Column(nullable=false) private LocalDateTime createdAt;
    @PrePersist void prePersist(){createdAt=LocalDateTime.now();}
}
