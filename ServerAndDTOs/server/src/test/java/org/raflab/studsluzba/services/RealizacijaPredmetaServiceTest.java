package org.raflab.studsluzba.services;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.repositories.ProgramPredmetRepository;
import org.raflab.studsluzba.repositories.RealizacijaPredmetaRepository;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class RealizacijaPredmetaServiceTest {

    @Test
    void enrollmentEnsuresOneAnnualRealizationForEveryProgramSubject() {
        RealizacijaPredmetaRepository realizacijaRepo = mock(RealizacijaPredmetaRepository.class);
        ProgramPredmetRepository programPredmetRepo = mock(ProgramPredmetRepository.class);
        SkolskaGodinaRepository schoolYearRepo = mock(SkolskaGodinaRepository.class);
        RealizacijaPredmetaService service = new RealizacijaPredmetaService(realizacijaRepo, programPredmetRepo, schoolYearRepo);

        SkolskaGodina sg = new SkolskaGodina();
        sg.setId(9L);
        ProgramPredmet first = new ProgramPredmet(); first.setId(1L);
        ProgramPredmet second = new ProgramPredmet(); second.setId(2L);

        when(schoolYearRepo.findFirstByAktivnaTrue()).thenReturn(sg);
        when(programPredmetRepo.findByProgramIdAndGodinaStudijaOrderBySemestarUGodini(5L, 1))
                .thenReturn(Arrays.asList(first, second));
        when(realizacijaRepo.findByProgramPredmetIdAndSkolskaGodinaId(any(), eq(9L))).thenReturn(Optional.empty());
        when(realizacijaRepo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(realizacijaRepo.findForEnrollment(5L, 1, 9L)).thenReturn(Collections.emptyList());

        assertThat(service.ensureForEnrollment(5L, 1)).isEmpty();
        verify(realizacijaRepo, times(2)).save(any(RealizacijaPredmeta.class));
    }
}
