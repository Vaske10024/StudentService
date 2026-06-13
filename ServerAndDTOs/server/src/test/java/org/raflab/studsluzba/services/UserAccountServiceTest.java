package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserAccountServiceTest {
    private final UserAccountRepository repository = mock(UserAccountRepository.class);
    private final CurrentUser currentUser = mock(CurrentUser.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserAccountService service = new UserAccountService(repository, passwordEncoder, currentUser);

    @Test
    void firstIndexCreatesStudentAccountWithFacultyEmailAsInitialPassword() {
        StudentPodaci student = student();
        StudentIndeks index = new StudentIndeks();
        index.setId(10L);
        when(repository.findStudentAccountByStudentPodaciId(5L)).thenReturn(Optional.empty());
        when(repository.existsByUsername("student@example.edu")).thenReturn(false);
        when(repository.save(any(UserAccount.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserAccount account = service.provisionStudentAccount(student, index);

        assertThat(account.getUsername()).isEqualTo("student@example.edu");
        assertThat(account.getRole()).isEqualTo(Role.STUDENT);
        assertThat(account.isEnabled()).isTrue();
        assertThat(account.getLinkedStudentPodaci()).isSameAs(student);
        assertThat(account.getLinkedStudentIndeks()).isSameAs(index);
        assertThat(passwordEncoder.matches("student@example.edu", account.getPasswordHash())).isTrue();
    }

    @Test
    void newIndexRelinksExistingAccountWithoutResettingPassword() {
        StudentPodaci student = student();
        StudentIndeks index = new StudentIndeks();
        index.setId(11L);
        UserAccount existing = new UserAccount();
        existing.setPasswordHash(passwordEncoder.encode("changed-password"));
        when(repository.findStudentAccountByStudentPodaciId(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        UserAccount account = service.provisionStudentAccount(student, index);

        assertThat(account.getLinkedStudentIndeks()).isSameAs(index);
        assertThat(passwordEncoder.matches("changed-password", account.getPasswordHash())).isTrue();
        verify(repository, never()).existsByUsername(any());
    }

    @Test
    void passwordChangeRequiresCorrectCurrentPassword() {
        UserAccount account = new UserAccount();
        account.setPasswordHash(passwordEncoder.encode("old-password"));
        when(currentUser.account()).thenReturn(account);

        assertThatThrownBy(() -> service.changeCurrentPassword("wrong-password", "new-password"))
                .isInstanceOf(ApiException.class)
                .hasMessage("Trenutna lozinka nije ispravna.");
        verify(repository, never()).save(any());
    }

    @Test
    void passwordChangeStoresNewHash() {
        UserAccount account = new UserAccount();
        account.setPasswordHash(passwordEncoder.encode("old-password"));
        when(currentUser.account()).thenReturn(account);

        service.changeCurrentPassword("old-password", "new-password");

        assertThat(passwordEncoder.matches("new-password", account.getPasswordHash())).isTrue();
        verify(repository).save(account);
    }

    private StudentPodaci student() {
        StudentPodaci student = new StudentPodaci();
        student.setId(5L);
        student.setEmailFakultetski("student@example.edu");
        return student;
    }
}
