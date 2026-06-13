package org.raflab.studsluzba.services;



import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.repositories.ProgramPredmetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional(readOnly = true)
public class ProgramService {
    private final ProgramPredmetRepository ppRepo;
    private final IspitQueryService iqService;

    public List<ProgramPredmet> predmeti(Long programId){
        return ppRepo.findProgramPredmeti(programId);
    }

    public Double prosekZaPredmetURasponu(Long predmetId, int fromYear, int toYear){
        return iqService.prosekZaPredmetURasponu(predmetId, fromYear, toYear);
    }
}
