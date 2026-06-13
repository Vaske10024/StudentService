package org.raflab.studsluzba.model.schedule;
import lombok.Getter;import lombok.Setter;import javax.persistence.*;
@Entity @Getter @Setter
public class StudentGroup{@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(nullable=false,unique=true)private String code;private String name;}
