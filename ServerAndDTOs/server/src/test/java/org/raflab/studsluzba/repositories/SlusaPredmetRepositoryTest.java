package org.raflab.studsluzba.repositories;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.VrstaStudija;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import javax.persistence.EntityManager;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class SlusaPredmetRepositoryTest {
    @Autowired private EntityManager em;
    @Autowired private SlusaPredmetRepository repository;

    @Test
    void professorStudentListFetchesPersonalDataWithRealizationBasedListening() {
        VrstaStudija vrsta = new VrstaStudija();
        vrsta.setSkracenica("OAS");
        vrsta.setPuniNaziv("Osnovne akademske studije");
        em.persist(vrsta);

        StudijskiProgram program = new StudijskiProgram();
        program.setOznaka("RN");
        program.setNaziv("Racunarske nauke");
        program.setGodinaAkreditacije(2024);
        program.setVrstaStudija(vrsta);
        em.persist(program);

        Predmet predmet = new Predmet();
        predmet.setSifra("PRG1");
        predmet.setNaziv("Programiranje 1");
        em.persist(predmet);

        ProgramPredmet programPredmet = new ProgramPredmet();
        programPredmet.setProgram(program);
        programPredmet.setPredmet(predmet);
        programPredmet.setGodinaStudija(1);
        programPredmet.setSemestarUGodini(1);
        em.persist(programPredmet);

        SkolskaGodina skolskaGodina = new SkolskaGodina();
        skolskaGodina.setGodina("2025/26");
        skolskaGodina.setAktivna(true);
        em.persist(skolskaGodina);

        RealizacijaPredmeta realizacija = new RealizacijaPredmeta();
        realizacija.setProgramPredmet(programPredmet);
        realizacija.setSkolskaGodina(skolskaGodina);
        realizacija.setStatus(RealizacijaPredmeta.Status.ACTIVE);
        em.persist(realizacija);

        DrziPredmet dodela = new DrziPredmet();
        dodela.setPredmet(predmet);
        dodela.setSkolskaGodina(skolskaGodina);
        dodela.setRealizacijaPredmeta(realizacija);
        em.persist(dodela);

        StudentPodaci podaci = new StudentPodaci();
        podaci.setIme("Ana");
        podaci.setPrezime("Anic");
        em.persist(podaci);

        StudentIndeks indeks = new StudentIndeks();
        indeks.setStudent(podaci);
        indeks.setBroj(1);
        indeks.setGodina(2025);
        indeks.setStudProgramOznaka("RN");
        indeks.setAktivan(true);
        em.persist(indeks);

        SlusaPredmet slusanje = new SlusaPredmet();
        slusanje.setStudentIndeks(indeks);
        slusanje.setRealizacijaPredmeta(realizacija);
        slusanje.setSkolskaGodina(skolskaGodina);
        em.persist(slusanje);
        em.flush();
        em.clear();

        List<StudentIndeks> studenti = repository.getStudentiSlusaPredmetZaDrziPredmet(dodela.getId());
        em.clear();

        assertThat(studenti).hasSize(1);
        assertThat(studenti.get(0).getStudent().getIme()).isEqualTo("Ana");
        assertThat(studenti.get(0).getStudent().getPrezime()).isEqualTo("Anic");
    }
}
