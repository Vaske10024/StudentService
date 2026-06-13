package org.raflab.studsluzba.model.security;
import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.time.LocalDateTime;
@Entity @Getter @Setter
public class SystemSetting {
 @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
 @Column(nullable=false,unique=true) private String settingKey;
 @Column(nullable=false,length=2000) private String settingValue;
 private String description;
 private Long updatedByUserId;
 @Column(nullable=false) private LocalDateTime updatedAt;
 @PrePersist @PreUpdate void touch(){updatedAt=LocalDateTime.now();}
}
