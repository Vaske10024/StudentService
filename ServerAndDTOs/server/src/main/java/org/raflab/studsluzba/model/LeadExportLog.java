package org.raflab.studsluzba.model;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "lead_export_log")
public class LeadExportLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exported_by_user_id", nullable = false)
    private UserAccount exportedBy;

    @Enumerated(EnumType.STRING)
    @Column(name = "exporter_role", nullable = false, length = 32)
    private Role exporterRole;

    @Column(name = "export_type", nullable = false, length = 80)
    private String exportType;

    @Column(nullable = false)
    private boolean masked;

    @Column(name = "record_count", nullable = false)
    private int recordCount;

    @Column(length = 1000)
    private String filters;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "user_agent", length = 255)
    private String userAgent;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
