package org.raflab.studsluzba.model.ispiti;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatCode;

class SchoolYearEntityIdentityTest {

    @Test
    void cyclicSchoolYearRelationshipsDoNotRecurseWhileHashing() {
        SkolskaGodina schoolYear = new SkolskaGodina();

        DrziPredmet assignment = new DrziPredmet();
        assignment.setSkolskaGodina(schoolYear);

        SlusaPredmet listening = new SlusaPredmet();
        listening.setSkolskaGodina(schoolYear);

        Set<DrziPredmet> assignments = new HashSet<>();
        assignments.add(assignment);
        schoolYear.setPredmeti(assignments);

        Set<SlusaPredmet> listenings = new HashSet<>();
        listenings.add(listening);
        schoolYear.setListaSlusanjaPredmeta(listenings);

        assertThatCode(schoolYear::hashCode).doesNotThrowAnyException();
        assertThatCode(assignment::hashCode).doesNotThrowAnyException();
        assertThatCode(listening::hashCode).doesNotThrowAnyException();
    }
}
