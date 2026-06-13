package org.raflab.studsluzba.model.notification;
import com.fasterxml.jackson.annotation.JsonIgnore;import lombok.Getter;import lombok.Setter;import org.raflab.studsluzba.model.security.UserAccount;import javax.persistence.*;import java.time.LocalDateTime;
@Entity @Getter @Setter
public class Notification{@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@JsonIgnore @ManyToOne(optional=false)private UserAccount recipient;@Column(nullable=false,length=80)private String type;@Column(nullable=false)private String title;@Column(nullable=false,length=2000)private String message;@Column(nullable=false)private boolean readFlag;@Column(nullable=false)private LocalDateTime createdAt;@PrePersist void p(){createdAt=LocalDateTime.now();}}
