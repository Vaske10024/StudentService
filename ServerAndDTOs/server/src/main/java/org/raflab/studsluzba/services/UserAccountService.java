package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Transactional
    public UserAccount provisionStudentAccount(StudentPodaci student, StudentIndeks activeIndex) {
        String facultyEmail = clean(student.getEmailFakultetski());
        if (facultyEmail == null) {
            throw ApiException.badRequest("Fakultetski email je obavezan za kreiranje studentskog naloga.");
        }

        UserAccount account = userAccountRepository.findStudentAccountByStudentPodaciId(student.getId())
                .orElse(null);
        if (account == null) {
            if (userAccountRepository.existsByUsername(facultyEmail)) {
                throw ApiException.conflict("USERNAME_EXISTS", "Korisnicki nalog sa fakultetskim email-om vec postoji.");
            }
            account = new UserAccount();
            account.setUsername(facultyEmail);
            account.setPasswordHash(passwordEncoder.encode(facultyEmail));
            account.setRole(Role.STUDENT);
            account.setEnabled(true);
            account.setLinkedStudentPodaci(student);

            // TODO: Generate a temporary password and send it to the student's private email.
        }

        account.setLinkedStudentIndeks(activeIndex);
        return userAccountRepository.save(account);
    }

    @Transactional
    public void changeCurrentPassword(String currentPassword, String newPassword) {
        UserAccount account = currentUser.account();
        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw ApiException.badRequest("Trenutna lozinka nije ispravna.");
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        userAccountRepository.save(account);
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
