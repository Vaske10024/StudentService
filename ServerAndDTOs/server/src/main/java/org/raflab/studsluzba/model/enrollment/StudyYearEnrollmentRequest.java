package org.raflab.studsluzba.model.enrollment;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.ObnovaGodine;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.UpisGodine;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Getter
@Setter
@Table(name = "study_year_enrollment_request")
public class StudyYearEnrollmentRequest {

    public enum Type {
        ENROLL_NEXT_YEAR,
        RENEW_YEAR,
        CONDITIONAL_ENROLLMENT
    }

    public enum Status {
        SUBMITTED,
        PENDING_DOCUMENTS,
        PENDING_ADMIN_APPROVAL,
        APPROVED,
        REJECTED,
        NEEDS_CHANGES,
        CANCELLED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "student_indeks_id", nullable = false)
    private StudentIndeks studentIndeks;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "current_school_year_id", nullable = false)
    private SkolskaGodina currentSchoolYear;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "target_school_year_id", nullable = false)
    private SkolskaGodina targetSchoolYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Type type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 40)
    private Status status = Status.SUBMITTED;

    @Column(nullable = false)
    private Integer currentStudyYear;

    @Column(nullable = false)
    private Integer requestedStudyYear;

    @Column(nullable = false)
    private Integer earnedEctsSnapshot;

    @Column(nullable = false)
    private boolean contractReceived;

    @Column(nullable = false)
    private boolean paymentConfirmed;

    @Column(nullable = false)
    private boolean documentationComplete;

    @Column(length = 2000)
    private String studentNote;

    @Column(length = 2000)
    private String adminNote;

    private Long submittedByUserId;
    private Long decidedByUserId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_upis_godine_id")
    private UpisGodine approvedEnrollment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_obnova_godine_id")
    private ObnovaGodine approvedRenewal;

    @Column(nullable = false, updatable = false)
    private LocalDateTime submittedAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    private LocalDateTime decidedAt;

    @OneToMany(mappedBy = "request", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<StudyYearEnrollmentTransferredSubject> transferredSubjects = new LinkedHashSet<>();

    @PrePersist
    void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        submittedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
