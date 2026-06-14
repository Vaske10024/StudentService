package org.raflab.studsluzba.repositories.security;

import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.model.security.Role;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
    @EntityGraph(attributePaths = {"linkedStudentPodaci", "linkedStudentIndeks", "linkedNastavnik"})
    Optional<UserAccount> findByUsername(String username);

    @EntityGraph(attributePaths = {"linkedStudentPodaci", "linkedStudentIndeks", "linkedNastavnik"})
    Optional<UserAccount> findByLinkedStudentIndeksId(Long indeksId);

    @EntityGraph(attributePaths = {"linkedStudentPodaci", "linkedStudentIndeks", "linkedNastavnik"})
    @Query("select ua from UserAccount ua where ua.linkedStudentPodaci.id = :studentPodaciId and ua.role = org.raflab.studsluzba.model.security.Role.STUDENT")
    Optional<UserAccount> findStudentAccountByStudentPodaciId(@Param("studentPodaciId") Long studentPodaciId);

    boolean existsByUsername(String username);
    boolean existsByRole(Role role);

    @EntityGraph(attributePaths = {"linkedNastavnik"})
    Optional<UserAccount> findByLinkedNastavnikId(Long nastavnikId);
}
