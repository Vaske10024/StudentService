package org.raflab.studsluzba.model.security;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.PotentialStudentLead;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "audit_log")
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long actorUserId;

    @Column(length = 180)
    private String actorUsername;

    @Enumerated(EnumType.STRING)
    @Column(length = 32)
    private Role actorRole;

    @ManyToOne
    @JoinColumn(name = "lead_id")
    private PotentialStudentLead lead;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(length = 2000)
    private String details;

    @Column(name = "old_value", length = 1000)
    private String oldValue;

    @Column(name = "new_value", length = 1000)
    private String newValue;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
