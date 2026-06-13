package org.raflab.studsluzba.model.academic;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
@Entity @Getter @Setter
public class AcademicRule {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(nullable=false, unique=true) private String ruleKey;
    @Column(nullable=false, length=1000) private String ruleValue;
    @Column(nullable=false) private boolean active=true;
}
