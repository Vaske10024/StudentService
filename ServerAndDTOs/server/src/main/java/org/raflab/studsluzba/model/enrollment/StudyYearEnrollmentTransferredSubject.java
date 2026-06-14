package org.raflab.studsluzba.model.enrollment;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.ispiti.Predmet;

import javax.persistence.*;

@Entity
@Getter
@Setter
@Table(name = "study_year_enrollment_transferred_subject",
        uniqueConstraints = @UniqueConstraint(name = "uk_year_request_subject",
                columnNames = {"request_id", "subject_id"}))
public class StudyYearEnrollmentTransferredSubject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "request_id", nullable = false)
    private StudyYearEnrollmentRequest request;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "subject_id", nullable = false)
    private Predmet subject;

    @Column(nullable = false)
    private Integer ectsSnapshot;
}
