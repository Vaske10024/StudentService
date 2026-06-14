package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.PredispitnaObaveza;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.OstvarenaPredObavRepository;
import org.raflab.studsluzba.repositories.PredispitnaObavezaRepository;
import org.raflab.studsluzba.repositories.SlusaPredmetRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.security.ApiException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OstvarenaPredObavServiceTest {
    private final OstvarenaPredObavRepository scoreRepo = mock(OstvarenaPredObavRepository.class);
    private final PredispitnaObavezaRepository definitionRepo = mock(PredispitnaObavezaRepository.class);
    private final StudentIndeksRepository indeksRepo = mock(StudentIndeksRepository.class);
    private final IspitRepository ispitRepo = mock(IspitRepository.class);
    private final SlusaPredmetRepository slusaRepo = mock(SlusaPredmetRepository.class);
    private final DrziPredmetRepository drziRepo = mock(DrziPredmetRepository.class);
    private final OstvarenaPredObavService service = new OstvarenaPredObavService(
            scoreRepo, definitionRepo, indeksRepo, ispitRepo, slusaRepo, drziRepo);

    @Test
    void scoreCannotBeAddedForStudentWhoDoesNotListenToSubject() {
        StudentIndeks indeks = new StudentIndeks();
        indeks.setId(10L);
        PredispitnaObaveza definition = definition(20L, 30L, 40L);
        when(indeksRepo.findById(10L)).thenReturn(Optional.of(indeks));
        when(definitionRepo.findById(20L)).thenReturn(Optional.of(definition));
        when(slusaRepo.existsStudentSlusaPredmetUGodini(10L, 30L, 40L)).thenReturn(false);

        assertThatThrownBy(() -> service.upsert(10L, 20L, 5))
                .isInstanceOfSatisfying(ApiException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("STUDENT_NOT_ENROLLED_IN_SUBJECT"));
        verify(scoreRepo, never()).save(any());
    }

    private PredispitnaObaveza definition(Long id, Long predmetId, Long schoolYearId) {
        Predmet predmet = new Predmet();
        predmet.setId(predmetId);
        SkolskaGodina schoolYear = new SkolskaGodina();
        schoolYear.setId(schoolYearId);
        PredispitnaObaveza definition = new PredispitnaObaveza();
        definition.setId(id);
        definition.setPredmet(predmet);
        definition.setSkolskaGodina(schoolYear);
        definition.setMaxPoeni(10);
        return definition;
    }
}
