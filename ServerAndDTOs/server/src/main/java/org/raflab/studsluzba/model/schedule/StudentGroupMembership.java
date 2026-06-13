package org.raflab.studsluzba.model.schedule;
import lombok.Getter;import lombok.Setter;import org.raflab.studsluzba.model.StudentIndeks;import javax.persistence.*;
@Entity @Getter @Setter @Table(uniqueConstraints=@UniqueConstraint(name="uk_group_membership",columnNames={"student_group_id","student_indeks_id"}))
public class StudentGroupMembership{@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@ManyToOne(optional=false)private StudentGroup studentGroup;@ManyToOne(optional=false)private StudentIndeks studentIndeks;}
