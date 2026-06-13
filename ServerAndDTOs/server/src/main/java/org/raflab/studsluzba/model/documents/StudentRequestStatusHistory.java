package org.raflab.studsluzba.model.documents;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
public class StudentRequestStatusHistory {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private StudentRequest studentRequest;
    @Column(nullable = false, length = 32) private String oldStatus;
    @Column(nullable = false, length = 32) private String newStatus;
    private String note;
    private Long actorUserId;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
