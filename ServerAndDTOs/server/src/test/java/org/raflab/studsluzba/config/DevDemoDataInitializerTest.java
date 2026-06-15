package org.raflab.studsluzba.config;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.security.UserAccount;
import org.raflab.studsluzba.repositories.security.SystemSettingRepository;
import org.raflab.studsluzba.repositories.security.UserAccountRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.services.DebtPolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(properties = {
        "app.seed.demo-data.enabled=true",
        "spring.datasource.url=jdbc:h2:mem:demo-seed;DB_CLOSE_DELAY=-1;MODE=MySQL;DATABASE_TO_UPPER=false",
        "spring.datasource.driverClassName=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create",
        "spring.flyway.enabled=false"
})
@ActiveProfiles({"test", "dev"})
class DevDemoDataInitializerTest {
    @Autowired DevDemoDataInitializer initializer;
    @Autowired UserAccountRepository accountRepo;
    @Autowired SystemSettingRepository settingRepo;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired AuthenticationManager authenticationManager;
    @Autowired DebtPolicyService debtPolicyService;
    @Autowired EntityManager em;

    @Test
    void seedsCompleteDatasetAndSecondRunIsIdempotent() throws Exception {
        long accounts = count("UserAccount");
        long subjects = count("Predmet");
        long registrations = count("PrijavaIspita");
        long requests = count("StudyYearEnrollmentRequest");

        initializer.run(null);

        assertThat(count("UserAccount")).isEqualTo(accounts);
        assertThat(count("Predmet")).isEqualTo(subjects);
        assertThat(count("PrijavaIspita")).isEqualTo(registrations);
        assertThat(count("StudyYearEnrollmentRequest")).isEqualTo(requests);
        assertThat(settingRepo.findBySettingKey("dev.demo.seed.version")).isPresent();

        assertLoginReady("admin@demo.edu");
        assertLoginReady("marko.aleksic@demo.edu");
        assertLoginReady("student.freshman@demo.edu");
        assertThat(subjects).isGreaterThanOrEqualTo(40);
        assertThat(registrations).isGreaterThan(20);
        assertThat(requests).isGreaterThanOrEqualTo(5);
        assertThat(count("RealizacijaPredmeta")).isGreaterThanOrEqualTo(80);
        assertThat(count("DrziPredmet")).isGreaterThanOrEqualTo(40);
        assertThat(count("IspitniRok")).isGreaterThanOrEqualTo(3);
        assertThat(count("Ispit")).isGreaterThanOrEqualTo(50);
        assertThat(count("PredispitnaObaveza")).isGreaterThanOrEqualTo(120);
        assertThat(count("LedgerEntry")).isGreaterThanOrEqualTo(12);
        assertThat(count("StudentRequest")).isGreaterThanOrEqualTo(4);
        assertThat(count("Notification")).isGreaterThanOrEqualTo(5);

        assertThat(index("student.good@demo.edu").getOstvarenoEspb()).isEqualTo(96);
        assertThat(index("student.conditional@demo.edu").getOstvarenoEspb()).isEqualTo(42);
        assertThat(index("student.renewal@demo.edu").getOstvarenoEspb()).isEqualTo(18);
        assertThat(countFor("SlusaPredmet", "e.studentIndeks.id", index("student.freshman@demo.edu").getId())).isEqualTo(10);
        assertThat(countFor("DrziPredmet", "e.nastavnik.id", professor("marko.aleksic@demo.edu"))).isGreaterThan(0);
        assertThat(countFor("DrziPredmet", "e.nastavnik.id", professor("jelena.jovanovic@demo.edu"))).isGreaterThan(0);
        assertThat(countFor("DrziPredmet", "e.nastavnik.id", professor("nikola.petrovic@demo.edu"))).isGreaterThan(0);
        assertThatThrownBy(() -> debtPolicyService.assertExamRegistrationAllowed(index("student.debt@demo.edu").getId()))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("DEBT_BLOCKS_EXAM_REGISTRATION");
    }

    private void assertLoginReady(String username) {
        UserAccount account = accountRepo.findByUsername(username).orElseThrow(AssertionError::new);
        assertThat(account.isEnabled()).isTrue();
        assertThat(account.isMustChangePassword()).isFalse();
        assertThat(passwordEncoder.matches(DevDemoDataInitializer.PASSWORD, account.getPasswordHash())).isTrue();
        assertThat(authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, DevDemoDataInitializer.PASSWORD)).isAuthenticated()).isTrue();
    }

    private long count(String entity) {
        return em.createQuery("select count(e) from " + entity + " e", Long.class).getSingleResult();
    }

    private long countFor(String entity, String field, Long id) {
        return em.createQuery("select count(e) from " + entity + " e where " + field + "=:id", Long.class)
                .setParameter("id", id).getSingleResult();
    }

    private org.raflab.studsluzba.model.StudentIndeks index(String username) {
        return accountRepo.findByUsername(username).orElseThrow(AssertionError::new).getLinkedStudentIndeks();
    }

    private Long professor(String username) {
        return accountRepo.findByUsername(username).orElseThrow(AssertionError::new).getLinkedNastavnik().getId();
    }
}
