package org.raflab.studsluzba.model.notification;
import lombok.Getter;import lombok.Setter;import javax.persistence.*;
@Entity @Getter @Setter
public class NotificationTemplate{@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(nullable=false,unique=true)private String templateKey;@Column(nullable=false)private String subject;@Column(nullable=false,length=4000)private String body;}
