package org.raflab.studsluzba.model.academic;
import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(name="uk_graduation_indeks", columnNames="student_indeks_id"))
public class GraduationRecord {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @OneToOne(optional=false) private StudentIndeks studentIndeks;
    @Column(nullable=false) private Integer earnedEcts;
    @Column(nullable=false) private Double averageGrade;
    private String note;
    private Long approvedByUserId;
    @Column(nullable=false) private LocalDateTime graduatedAt;
    @PrePersist void prePersist(){ graduatedAt=LocalDateTime.now(); }
}
