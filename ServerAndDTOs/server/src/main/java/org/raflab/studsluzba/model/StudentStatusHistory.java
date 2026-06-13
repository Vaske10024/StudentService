package org.raflab.studsluzba.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "student_status_history")
public class StudentStatusHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_indeks_id", nullable = false)
    private StudentIndeks studentIndeks;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_status", length = 32)
    private StudentStatus oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_status", nullable = false, length = 32)
    private StudentStatus newStatus;

    @Column(length = 1000)
    private String reason;

    @Column(nullable = false)
    private LocalDate validFrom;

    private LocalDate validTo;
    private Long changedByUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        if (validFrom == null) validFrom = LocalDate.now();
        createdAt = LocalDateTime.now();
    }
}
