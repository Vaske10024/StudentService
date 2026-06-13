package org.raflab.studsluzba.model.schedule;
import lombok.Getter;import lombok.Setter;import javax.persistence.*;
@Entity @Getter @Setter
public class Room{@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(nullable=false,unique=true)private String code;@Column(nullable=false)private int capacity;private String location;}
