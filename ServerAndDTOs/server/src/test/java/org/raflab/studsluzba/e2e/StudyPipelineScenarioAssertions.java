package org.raflab.studsluzba.e2e;

import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.ispiti.PrijavaStatus;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

final class StudyPipelineScenarioAssertions {
    private StudyPipelineScenarioAssertions() {
    }

    static void assertUniqueListeningRecords(List<SlusaPredmet> records) {
        assertThat(records).extracting(item -> item.getRealizacijaPredmeta().getId()).doesNotHaveDuplicates();
    }

    static void assertAttemptHistory(List<PrijavaIspita> attempts, PrijavaStatus... statuses) {
        assertThat(attempts).extracting(PrijavaIspita::getStatus).contains(statuses);
    }
}
