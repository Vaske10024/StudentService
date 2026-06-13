package org.raflab.studsluzba.model.academic;
import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.Predmet;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(name="uk_recognized_subject",columnNames={"student_indeks_id","subject_id"}))
public class RecognizedSubject {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) private StudentIndeks studentIndeks;
    @ManyToOne(optional=false) @JoinColumn(name="subject_id") private Predmet subject;
    private Integer grade;
    private Integer ects;
    @Column(nullable=false) private String source;
    private Long approvedByUserId;
    @Column(nullable=false) private LocalDateTime createdAt;
    @PrePersist void prePersist(){createdAt=LocalDateTime.now();}
}
