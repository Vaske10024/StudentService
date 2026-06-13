package org.raflab.studsluzba.model.documents;
import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
public class StudentDocument {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private StudentIndeks studentIndeks;
    @ManyToOne private StudentRequest studentRequest;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private DocumentType type;
    @Column(nullable = false) private String originalName;
    @Column(nullable = false) private String contentType;
    @Column(nullable = false) private long sizeBytes;
    @Column(nullable = false, unique = true) private String storageKey;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
