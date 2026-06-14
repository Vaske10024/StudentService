package org.raflab.studsluzba.model.enrollment;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "study_year_enrollment_request_history")
public class StudyYearEnrollmentRequestHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private StudyYearEnrollmentRequest request;

    @Enumerated(EnumType.STRING)
    @Column(length = 40)
    private StudyYearEnrollmentRequest.Status oldStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private StudyYearEnrollmentRequest.Status newStatus;

    @Column(length = 2000)
    private String note;

    private Long actorUserId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void prePersist() {
        createdAt = LocalDateTime.now();
    }
}
