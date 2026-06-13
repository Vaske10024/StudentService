package org.raflab.studsluzba.model.notification;
import lombok.Getter;import lombok.Setter;import javax.persistence.*;import java.time.LocalDateTime;
@Entity @Getter @Setter
public class EmailOutbox{public enum Status{PENDING,SENT,FAILED}@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(nullable=false)private String recipientEmail;@Column(nullable=false)private String subject;@Column(nullable=false,length=4000)private String body;@Enumerated(EnumType.STRING)@Column(nullable=false,length=32)private Status status=Status.PENDING;@Column(nullable=false)private LocalDateTime createdAt;@PrePersist void p(){createdAt=LocalDateTime.now();}}
