package org.raflab.studsluzba.repositories.enrollment;

import org.raflab.studsluzba.model.enrollment.StudyYearEnrollmentRequest;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface StudyYearEnrollmentRequestRepository extends JpaRepository<StudyYearEnrollmentRequest, Long> {

    boolean existsByStudentIndeksIdAndTargetSchoolYearIdAndStatusIn(
            Long studentIndeksId,
            Long targetSchoolYearId,
            Collection<StudyYearEnrollmentRequest.Status> statuses);

    @EntityGraph(attributePaths = {"studentIndeks", "studentIndeks.student", "currentSchoolYear",
            "targetSchoolYear", "transferredSubjects", "transferredSubjects.subject",
            "approvedEnrollment", "approvedRenewal"})
    List<StudyYearEnrollmentRequest> findByStudentIndeksIdOrderBySubmittedAtDesc(Long studentIndeksId);

    @EntityGraph(attributePaths = {"studentIndeks", "studentIndeks.student", "currentSchoolYear",
            "targetSchoolYear", "transferredSubjects", "transferredSubjects.subject",
            "approvedEnrollment", "approvedRenewal"})
    @Query("select distinct r from StudyYearEnrollmentRequest r " +
            "where (:status is null or r.status = :status) " +
            "and (:type is null or r.type = :type) " +
            "and (:targetSchoolYearId is null or r.targetSchoolYear.id = :targetSchoolYearId) " +
            "and (:studentIndeksId is null or r.studentIndeks.id = :studentIndeksId) " +
            "order by r.submittedAt desc")
    List<StudyYearEnrollmentRequest> search(
            @Param("status") StudyYearEnrollmentRequest.Status status,
            @Param("type") StudyYearEnrollmentRequest.Type type,
            @Param("targetSchoolYearId") Long targetSchoolYearId,
            @Param("studentIndeksId") Long studentIndeksId);
}
