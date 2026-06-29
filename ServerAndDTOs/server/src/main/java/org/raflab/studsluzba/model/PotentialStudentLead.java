package org.raflab.studsluzba.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "potential_student_lead")
public class PotentialStudentLead {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, length = 180)
    private String email;

    @Column(length = 60)
    private String phone;

    @Column(name = "interested_program", length = 180)
    private String interestedProgram;

    @Column(length = 180)
    private String source;

    @Column(length = 1000)
    private String note;

    @Column(name = "privacy_consent", nullable = false)
    private boolean privacyConsent = true;

    @Column(name = "consent_at", nullable = false)
    private LocalDateTime consentAt;

    @Column(name = "remote_address", length = 64)
    private String remoteAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private LeadStatus status = LeadStatus.NEW;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        if (consentAt == null) {
            consentAt = now;
        }
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
