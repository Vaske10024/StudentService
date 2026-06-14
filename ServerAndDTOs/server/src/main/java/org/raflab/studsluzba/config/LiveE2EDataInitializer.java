package org.raflab.studsluzba.config;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.StudentStatus;
import org.raflab.studsluzba.model.security.Role;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.time.LocalDate;

@Component
@Profile("e2e")
@RequiredArgsConstructor
public class LiveE2EDataInitializer implements ApplicationRunner {
    public static final String PASSWORD = "E2E-Role-Password-123!";

    private final EntityManager entityManager;
    private final UserAccountRepository accountRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!accountRepo.existsByUsername("e2e.student@example.test")) createStudent();
        if (!accountRepo.existsByUsername("e2e.professor@example.test")) createProfessor();
    }

    private void createStudent() {
        StudentPodaci student = new StudentPodaci();
        student.setIme("E2E");
        student.setPrezime("Student");
        student.setJmbg("E2E_LIVE_STUDENT");
        student.setEmailFakultetski("e2e.student@example.test");
        student.setEmailPrivatni("e2e.student.private@example.test");
        entityManager.persist(student);

        StudentIndeks index = new StudentIndeks();
        index.setStudent(student);
        index.setStudProgramOznaka("E2E");
        index.setGodina(2100);
        index.setBroj(1);
        index.setAktivan(true);
        index.setStatus(StudentStatus.AKTIVAN);
        index.setOstvarenoEspb(0);
        index.setVaziOd(LocalDate.now());
        entityManager.persist(index);
        entityManager.flush();

        accountRepo.save(account("e2e.student@example.test", Role.STUDENT, index, null, student));
    }

    private void createProfessor() {
        Nastavnik professor = new Nastavnik();
        professor.setIme("E2E");
        professor.setPrezime("Professor");
        professor.setSrednjeIme("Live");
        professor.setEmail("e2e.professor@example.test");
        professor.setJmbg("E2E_LIVE_PROFESSOR");
        entityManager.persist(professor);
        entityManager.flush();

        accountRepo.save(account("e2e.professor@example.test", Role.PROFESSOR, null, professor, null));
    }

    private UserAccount account(String username, Role role, StudentIndeks index, Nastavnik professor,
                                StudentPodaci student) {
        UserAccount account = new UserAccount();
        account.setUsername(username);
        account.setPasswordHash(passwordEncoder.encode(PASSWORD));
        account.setRole(role);
        account.setEnabled(true);
        account.setMustChangePassword(false);
        account.setLinkedStudentIndeks(index);
        account.setLinkedNastavnik(professor);
        account.setLinkedStudentPodaci(student);
        return account;
    }
}
