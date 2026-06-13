package org.raflab.studsluzba.model;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "student_status_request")
public class StudentStatusRequest {
    public enum Type { MIROVANJE, ISPIS }
    public enum Status { PENDING, APPROVED, REJECTED }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_indeks_id", nullable = false)
    private StudentIndeks studentIndeks;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private Status status = Status.PENDING;

    @Column(nullable = false, length = 2000)
    private String reason;

    private LocalDate requestedFrom;
    private LocalDate requestedTo;
    private Long submittedByUserId;
    private Long decidedByUserId;

    @Column(length = 2000)
    private String decisionNote;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    private LocalDateTime decidedAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
