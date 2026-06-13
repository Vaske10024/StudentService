package org.raflab.studsluzba.model.documents;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
public class GeneratedCertificate {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @OneToOne(optional = false) private StudentRequest studentRequest;
    @Column(nullable = false, unique = true) private String storageKey;
    @Column(nullable = false) private String verificationCode;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
