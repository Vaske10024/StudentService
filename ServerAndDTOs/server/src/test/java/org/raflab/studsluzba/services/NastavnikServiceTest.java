package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.NastavnikZvanje;
import org.raflab.studsluzba.model.dtos.NastavnikResponse;
import org.raflab.studsluzba.repositories.NastavnikRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(NastavnikService.class)
class NastavnikServiceTest {
    @Autowired EntityManager em;
    @Autowired NastavnikService service;
    @MockBean UserAccountService userAccountService;

    @Test
    void detailsConvertsLazyTitlesInsideTransaction() {
        Nastavnik professor = new Nastavnik();
        professor.setIme("Demo");
        professor.setPrezime("Profesor");
        professor.setEmail("details.professor@example.test");
        professor.setJmbg("DETAILS_PROFESSOR");
        em.persist(professor);

        NastavnikZvanje title = new NastavnikZvanje();
        title.setNastavnik(professor);
        title.setZvanje("Docent");
        title.setDatumIzbora(LocalDate.now());
        title.setAktivno(true);
        em.persist(title);
        em.flush();
        em.clear();

        NastavnikResponse response = service.details(professor.getId());

        assertThat(response).isNotNull();
        assertThat(response.getZvanja()).extracting("zvanje").containsExactly("Docent");
    }
}
