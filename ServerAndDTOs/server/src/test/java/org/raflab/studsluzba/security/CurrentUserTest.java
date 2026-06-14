package org.raflab.studsluzba.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.PredispitnaObaveza;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.repositories.schedule.StudentGroupMembershipRepository;
import org.raflab.studsluzba.repositories.schedule.ClassSessionRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CurrentUserTest {
    private final UserAccountRepository userRepo = mock(UserAccountRepository.class);
    private final StudentIndeksRepository indeksRepo = mock(StudentIndeksRepository.class);
    private final DrziPredmetRepository drziRepo = mock(DrziPredmetRepository.class);
    private final IspitRepository ispitRepo = mock(IspitRepository.class);
    private final IspitQueryRepository prijavaRepo = mock(IspitQueryRepository.class);
    private final PredispitnaObavezaRepository predObRepo = mock(PredispitnaObavezaRepository.class);
    private final CurrentUser currentUser = new CurrentUser(userRepo, indeksRepo, drziRepo, ispitRepo, prijavaRepo, predObRepo,
            mock(StudentGroupMembershipRepository.class), mock(ClassSessionRepository.class));

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void studentCanAccessOnlyOwnedIndex() {
        login("student");
        StudentPodaci podaci = new StudentPodaci();
        podaci.setId(5L);
        StudentIndeks own = new StudentIndeks();
        own.setId(10L);
        UserAccount ua = user("student", Role.STUDENT);
        ua.setLinkedStudentPodaci(podaci);
        when(userRepo.findByUsername("student")).thenReturn(Optional.of(ua));
        when(indeksRepo.findStudentIndeksiForStudentPodaciId(5L)).thenReturn(Arrays.asList(own));

        assertThatCode(() -> currentUser.requireStudentOwnsIndeks(10L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> currentUser.requireStudentOwnsIndeks(99L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void professorCanAccessOnlyAssignedTeachingAssignment() {
        login("prof");
        Nastavnik linked = new Nastavnik();
        linked.setId(7L);
        Nastavnik other = new Nastavnik();
        other.setId(8L);
        UserAccount ua = user("prof", Role.PROFESSOR);
        ua.setLinkedNastavnik(linked);
        when(userRepo.findByUsername("prof")).thenReturn(Optional.of(ua));
        DrziPredmet own = new DrziPredmet();
        own.setId(20L);
        own.setNastavnik(linked);
        DrziPredmet notOwn = new DrziPredmet();
        notOwn.setId(21L);
        notOwn.setNastavnik(other);
        when(drziRepo.findById(20L)).thenReturn(Optional.of(own));
        when(drziRepo.findById(21L)).thenReturn(Optional.of(notOwn));

        assertThatCode(() -> currentUser.requireProfessorOwnsDrziPredmet(20L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> currentUser.requireProfessorOwnsDrziPredmet(21L)).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void professorCanEditOnlyOwnExam() {
        login("prof");
        Nastavnik linked = new Nastavnik();
        linked.setId(7L);
        Nastavnik other = new Nastavnik();
        other.setId(8L);
        UserAccount ua = user("prof", Role.PROFESSOR);
        ua.setLinkedNastavnik(linked);
        when(userRepo.findByUsername("prof")).thenReturn(Optional.of(ua));

        DrziPredmet ownAssignment = new DrziPredmet();
        ownAssignment.setId(20L);
        ownAssignment.setNastavnik(linked);
        DrziPredmet otherAssignment = new DrziPredmet();
        otherAssignment.setId(21L);
        otherAssignment.setNastavnik(other);
        Ispit ownExam = new Ispit();
        ownExam.setDrziPredmet(ownAssignment);
        Ispit otherExam = new Ispit();
        otherExam.setDrziPredmet(otherAssignment);

        when(ispitRepo.findById(30L)).thenReturn(Optional.of(ownExam));
        when(ispitRepo.findById(31L)).thenReturn(Optional.of(otherExam));
        when(drziRepo.findById(20L)).thenReturn(Optional.of(ownAssignment));
        when(drziRepo.findById(21L)).thenReturn(Optional.of(otherAssignment));

        assertThatCode(() -> currentUser.requireAdminOrProfessorOwnsIspit(30L)).doesNotThrowAnyException();
        assertThatThrownBy(() -> currentUser.requireAdminOrProfessorOwnsIspit(31L))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void professorPreExamOwnershipUsesExistenceCheckInsteadOfSingleAssignmentQuery() {
        login("prof");
        Nastavnik linked = new Nastavnik();
        linked.setId(7L);
        UserAccount ua = user("prof", Role.PROFESSOR);
        ua.setLinkedNastavnik(linked);
        when(userRepo.findByUsername("prof")).thenReturn(Optional.of(ua));

        Predmet predmet = new Predmet();
        predmet.setId(30L);
        SkolskaGodina schoolYear = new SkolskaGodina();
        schoolYear.setId(40L);
        PredispitnaObaveza definition = new PredispitnaObaveza();
        definition.setPredmet(predmet);
        definition.setSkolskaGodina(schoolYear);
        when(predObRepo.findById(50L)).thenReturn(Optional.of(definition));
        when(drziRepo.existsProfessorAssignment(30L, 7L, 40L)).thenReturn(true);

        assertThatCode(() -> currentUser.requireAdminOrProfessorOwnsPredispitnaObaveza(50L))
                .doesNotThrowAnyException();
    }

    private void login(String username) {
        TestingAuthenticationToken auth = new TestingAuthenticationToken(username, "ignored", "ROLE_USER");
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private UserAccount user(String username, Role role) {
        UserAccount ua = new UserAccount();
        ua.setId(1L);
        ua.setUsername(username);
        ua.setRole(role);
        ua.setEnabled(true);
        return ua;
    }
}
