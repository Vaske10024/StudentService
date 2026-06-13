package org.raflab.studsluzba.utils;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.repositories.StudentPodaciRepository;

import java.util.Arrays;
import java.util.HashSet;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EntityMappersStudentPortalTest {

    @Test
    void studentSubjectContainsCurriculumDataAndAllInstructors() {
        Predmet subject = new Predmet();
        subject.setId(1L);
        subject.setSifra("RN101");
        subject.setNaziv("Programiranje 1");
        subject.setEspb(8);

        StudijskiProgram program = new StudijskiProgram();
        program.setId(2L);
        program.setOznaka("RN");

        ProgramPredmet programSubject = new ProgramPredmet();
        programSubject.setId(3L);
        programSubject.setPredmet(subject);
        programSubject.setProgram(program);
        programSubject.setGodinaStudija(1);
        programSubject.setSemestarUGodini(1);

        SkolskaGodina schoolYear = new SkolskaGodina();
        schoolYear.setGodina("2026/2027");

        RealizacijaPredmeta realization = new RealizacijaPredmeta();
        realization.setId(4L);
        realization.setProgramPredmet(programSubject);
        realization.setSkolskaGodina(schoolYear);
        realization.setStatus(RealizacijaPredmeta.Status.ACTIVE);
        realization.setAngazovanja(new HashSet<>(Arrays.asList(
                assignment("Ana", "Anic", DrziPredmet.Uloga.NOSILAC),
                assignment("Boris", "Boric", DrziPredmet.Uloga.PREDAVANJA),
                assignment("Ceda", "Cedic", DrziPredmet.Uloga.VEZBE)
        )));

        SlusaPredmet listening = new SlusaPredmet();
        listening.setId(5L);
        listening.setRealizacijaPredmeta(realization);

        var dto = new EntityMappers(mock(StudentPodaciRepository.class))
                .fromSlusaPredmetToStudentSubjectDTO(listening);

        assertThat(dto.getCode()).isEqualTo("RN101");
        assertThat(dto.getName()).isEqualTo("Programiranje 1");
        assertThat(dto.getEcts()).isEqualTo(8);
        assertThat(dto.getSemester()).isEqualTo(1);
        assertThat(dto.getInstructors()).hasSize(3)
                .extracting("nastavnikImePrezime")
                .containsExactlyInAnyOrder("Ana Anic", "Boris Boric", "Ceda Cedic");
    }

    private DrziPredmet assignment(String firstName, String lastName, DrziPredmet.Uloga role) {
        Nastavnik instructor = new Nastavnik();
        instructor.setIme(firstName);
        instructor.setPrezime(lastName);
        DrziPredmet assignment = new DrziPredmet();
        assignment.setNastavnik(instructor);
        assignment.setUloga(role);
        return assignment;
    }
}
