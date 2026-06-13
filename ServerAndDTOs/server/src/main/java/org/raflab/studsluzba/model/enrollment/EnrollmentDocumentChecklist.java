package org.raflab.studsluzba.model.enrollment;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
@Entity @Getter @Setter
public class EnrollmentDocumentChecklist {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private EnrollmentApplication application;
    @Column(nullable = false) private String documentType;
    @Column(nullable = false) private boolean verified;
}
