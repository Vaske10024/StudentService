package org.raflab.studsluzba.model.schedule;
import lombok.Getter;import lombok.Setter;import org.raflab.studsluzba.model.ispiti.Ispit;import javax.persistence.*;
@Entity @Getter @Setter @Table(uniqueConstraints=@UniqueConstraint(name="uk_exam_room",columnNames={"ispit_id","room_id"}))
public class ExamRoomAssignment{@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@ManyToOne(optional=false)private Ispit ispit;@ManyToOne(optional=false)private Room room;@Column(nullable=false)private int expectedStudents;}
