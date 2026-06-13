package org.raflab.studsluzba.model.schedule;
import lombok.Getter;import lombok.Setter;import org.raflab.studsluzba.model.Nastavnik;import javax.persistence.*;import java.time.LocalDateTime;
@Entity @Getter @Setter
public class ClassSession{@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(nullable=false)private String title;@ManyToOne(optional=false)private Room room;@ManyToOne(optional=false)private StudentGroup studentGroup;@ManyToOne(optional=false)private Nastavnik professor;@Column(nullable=false)private LocalDateTime startsAt;@Column(nullable=false)private LocalDateTime endsAt;}
