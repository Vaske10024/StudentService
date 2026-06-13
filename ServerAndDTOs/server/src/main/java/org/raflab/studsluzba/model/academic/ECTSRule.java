package org.raflab.studsluzba.model.academic;
import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import javax.persistence.*;
@Entity @Getter @Setter
@Table(uniqueConstraints=@UniqueConstraint(name="uk_ects_program_year", columnNames={"program_id","targetYear"}))
public class ECTSRule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional=false) @JoinColumn(name="program_id") private StudijskiProgram program;
    @Column(nullable=false) private Integer targetYear;
    @Column(nullable=false) private Integer minimumEcts;
}
