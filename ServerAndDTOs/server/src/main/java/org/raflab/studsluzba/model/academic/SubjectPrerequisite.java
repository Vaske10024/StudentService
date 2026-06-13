package org.raflab.studsluzba.model.academic;
import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.ispiti.Predmet;
import javax.persistence.*;
@Entity @Getter @Setter
@Table(uniqueConstraints = @UniqueConstraint(name="uk_subject_prerequisite", columnNames={"subject_id","prerequisite_id"}))
public class SubjectPrerequisite {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="subject_id") private Predmet subject;
    @ManyToOne(optional=false) @JoinColumn(name="prerequisite_id") private Predmet prerequisite;
}
