package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.VrstaStudija;
import org.raflab.studsluzba.model.dtos.StudijskiProgramCreateRequest;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.raflab.studsluzba.repositories.VrstaStudijaRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StudijskiProgramAdminService {
    private final StudijskiProgramRepository programRepo;
    private final VrstaStudijaRepository vrstaRepo;

    public Long create(StudijskiProgramCreateRequest request) {
        boolean duplicate = programRepo.findByOznaka(request.getOznaka()).stream()
                .anyMatch(program -> request.getGodinaAkreditacije().equals(program.getGodinaAkreditacije()));
        if (duplicate) {
            throw ApiException.conflict("STUDY_PROGRAM_EXISTS",
                    "Studijski program sa istom oznakom i godinom akreditacije vec postoji.");
        }
        VrstaStudija vrsta = vrstaRepo.findById(request.getVrstaStudijaId())
                .orElseThrow(() -> ApiException.notFound("Vrsta studija ne postoji: " + request.getVrstaStudijaId()));

        StudijskiProgram program = new StudijskiProgram();
        program.setOznaka(request.getOznaka().trim());
        program.setNaziv(request.getNaziv().trim());
        program.setGodinaAkreditacije(request.getGodinaAkreditacije());
        program.setZvanje(request.getZvanje().trim());
        program.setTrajanjeGodina(request.getTrajanjeGodina());
        program.setTrajanjeSemestara(request.getTrajanjeGodina() * 2);
        program.setUkupnoEspb(request.getUkupnoEspb());
        program.setVrstaStudija(vrsta);
        return programRepo.save(program).getId();
    }
}
