package org.raflab.studsluzba.controllers;



import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.StudentPodaciResponse;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.StudentPodaciRepository;
import org.raflab.studsluzba.utils.EntityMappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudentQueryFacade {
    //asd
    private final StudentIndeksRepository indeksRepo;
    private final StudentPodaciRepository spRepo;
    private final IspitQueryRepository iqRepo;
    private final EntityMappers mappers;

    public StudentPodaciResponse findPodaciByIndex(String sp, int godina, int broj){
        StudentIndeks si = indeksRepo.findStudentIndeks(sp, godina, broj);
        if (si == null) return null;
        StudentPodaci spEntity = si.getStudent();
        return mappers.fromStudentPodaciToResponse(spEntity);
    }

    public List<PrijavaIspita> polozeniByIndex(String sp, int godina, int broj){
        return iqRepo.polozeniByIndex(sp, godina, broj);
    }

    public List<StudentPodaciResponse> findBySrednja(String naziv){
        return spRepo.findBySrednjaSkola(naziv).stream()
                .map(mappers::fromStudentPodaciToResponse)
                .collect(Collectors.toList());
    }
}
