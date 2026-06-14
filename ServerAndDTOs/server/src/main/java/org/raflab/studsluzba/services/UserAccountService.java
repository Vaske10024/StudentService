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

import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class UserAccountService {
    private final UserAccountRepository userAccountRepository;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUser currentUser;

    @Transactional
    public UserAccount provisionStudentAccount(StudentPodaci student, StudentIndeks activeIndex) {
        return provisionStudentAccountWithCredential(student, activeIndex).getAccount();
    }

    @Transactional
    public ProvisionResult provisionStudentAccountWithCredential(StudentPodaci student, StudentIndeks activeIndex) {
        String facultyEmail = clean(student.getEmailFakultetski());
        if (facultyEmail == null) {
            throw ApiException.badRequest("Fakultetski email je obavezan za kreiranje studentskog naloga.");
        }

        UserAccount account = userAccountRepository.findStudentAccountByStudentPodaciId(student.getId())
                .orElse(null);
        String temporaryPassword = null;
        boolean created = false;
        if (account == null) {
            if (userAccountRepository.existsByUsername(facultyEmail)) {
                throw ApiException.conflict("USERNAME_EXISTS", "Korisnicki nalog sa fakultetskim email-om vec postoji.");
            }
            account = new UserAccount();
            temporaryPassword = generateTemporaryPassword();
            account.setUsername(facultyEmail);
            account.setPasswordHash(passwordEncoder.encode(temporaryPassword));
            account.setRole(Role.STUDENT);
            account.setEnabled(true);
            account.setMustChangePassword(true);
            account.setLinkedStudentPodaci(student);
            created = true;
        }

        account.setLinkedStudentIndeks(activeIndex);
        return new ProvisionResult(userAccountRepository.save(account), temporaryPassword, created);
    }

    @Transactional
    public void changeCurrentPassword(String currentPassword, String newPassword) {
        UserAccount account = currentUser.account();
        if (!passwordEncoder.matches(currentPassword, account.getPasswordHash())) {
            throw ApiException.badRequest("Trenutna lozinka nije ispravna.");
        }
        account.setPasswordHash(passwordEncoder.encode(newPassword));
        account.setMustChangePassword(false);
        userAccountRepository.save(account);
    }

    private String generateTemporaryPassword() {
        byte[] bytes = new byte[18];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    @lombok.Value
    public static class ProvisionResult {
        UserAccount account;
        String temporaryPassword;
        boolean created;
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }
}
