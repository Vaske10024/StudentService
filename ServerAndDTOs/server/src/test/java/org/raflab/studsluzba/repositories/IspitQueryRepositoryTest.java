package org.raflab.studsluzba.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class IspitQueryRepositoryTest {
    @Autowired private EntityManager em;
    @Autowired private IspitQueryRepository repo;

    @Test
    void prosecnaOcenaNaIspituFiltersByExamIdNotSubjectId() {
        Predmet p = predmet("PRG", "Programiranje");
        em.persist(p);
        DrziPredmet dp = new DrziPredmet();
        dp.setPredmet(p);
        em.persist(dp);

        Ispit examOne = ispit(dp, p);
        Ispit examTwo = ispit(dp, p);
        examTwo.setDatumOdrzavanja(LocalDate.now().plusDays(1));
        em.persist(examOne);
        em.persist(examTwo);

        StudentIndeks s1 = student(1);
        StudentIndeks s2 = student(2);
        StudentIndeks s3 = student(3);
        em.persist(s1.getStudent()); em.persist(s1);
        em.persist(s2.getStudent()); em.persist(s2);
        em.persist(s3.getStudent()); em.persist(s3);

        em.persist(prijava(examOne, s1, 6));
        em.persist(prijava(examOne, s2, 10));
        em.persist(prijava(examTwo, s3, 6));
        em.flush();

        assertThat(repo.prosecnaOcenaNaIspitu(examOne.getId())).isEqualTo(8.0d);
    }

    private Predmet predmet(String sifra, String naziv) {
        Predmet p = new Predmet();
        p.setSifra(sifra);
        p.setNaziv(naziv);
        return p;
    }

    private Ispit ispit(DrziPredmet dp, Predmet p) {
        Ispit i = new Ispit();
        i.setDrziPredmet(dp);
        i.setPredmet(p);
        i.setDatumOdrzavanja(LocalDate.now());
        return i;
    }

    private StudentIndeks student(int broj) {
        StudentPodaci sp = new StudentPodaci();
        sp.setIme("Student" + broj);
        sp.setPrezime("Test");
        StudentIndeks si = new StudentIndeks();
        si.setStudent(sp);
        si.setGodina(2024);
        si.setBroj(broj);
        si.setStudProgramOznaka("RN");
        si.setAktivan(true);
        return si;
    }

    private PrijavaIspita prijava(Ispit ispit, StudentIndeks student, int ocena) {
        PrijavaIspita pi = new PrijavaIspita();
        pi.setIspit(ispit);
        pi.setStudent(student);
        pi.setDatumPrijave(LocalDate.now());
        pi.setPonisteno(false);
        pi.setDaLiJeIzasao(true);
        pi.setBrojOsvojenihPoena(60);
        pi.setOcena(ocena);
        return pi;
    }
}
