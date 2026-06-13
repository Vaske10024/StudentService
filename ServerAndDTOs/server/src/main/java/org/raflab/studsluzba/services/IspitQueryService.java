package org.raflab.studsluzba.services;


import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.dtos.IspitRezultatDTO;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.utils.IspitMappers;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.raflab.studsluzba.utils.PrijavaMappers;
import org.raflab.studsluzba.model.dtos.PrijavaResponseDTO;

import org.springframework.transaction.annotation.Transactional;



@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IspitQueryService {

    private final IspitQueryRepository iqRepo;
    private final IspitRepository ispitRepo;
    private final PrijavaMappers prijavaMappers;
    private final PrijavaScoreService scoreService;
    private final IspitMappers ispitMappers;

    public List<StudentIndeks> listaPrijavljenih(Long ispitId) {
        return iqRepo.prijavljeniZaIspit(ispitId);
    }

    public Double prosecnaOcena(Long ispitId) {
        return iqRepo.prosecnaOcenaNaIspitu(ispitId);
    }

    public Long brojPolaganja(Long studentIndeksId, Long predmetId) {
        return iqRepo.brojPolaganja(studentIndeksId, predmetId);
    }

    public List<PrijavaIspita> rezultatiSortirani(Long ispitId) {
        return iqRepo.rezultatiSortirani(ispitId);
    }

    public Integer ukupniPoeniZaPrijavu(PrijavaIspita pi) {
        return scoreService.ukupniPoeniZaPrijavu(pi);
    }

    public Double prosekZaPredmetURasponu(Long predmetId, int fromYear, int toYear) {
        return iqRepo.prosecnaOcenaZaPredmetURasponu(predmetId, fromYear, toYear);
    }
    public List<IspitRezultatDTO> rezultatiSortiraniDTO(Long ispitId) {
        return iqRepo.rezultatiSortirani(ispitId).stream()
                .map(ispitMappers::toIspitRezultatDTO)
                .collect(Collectors.toList());
    }
    public List<PrijavaResponseDTO> aktivnePrijaveDTO(Long ispitId) {
        return iqRepo.aktivnePrijaveZaIspit(ispitId).stream()
                .map(prijavaMappers::toResponse)
                .collect(java.util.stream.Collectors.toList());
    }
}
