package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.raflab.studsluzba.repositories.ProgramPredmetRepository;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PredmetAdminService {

    private final PredmetRepository predmetRepo;
    private final StudijskiProgramRepository programRepo;
    private final ProgramPredmetRepository programPredmetRepo;

    public Long create(String sifra, String naziv, String opis, Integer espb, Long programId, boolean obavezan,
                       Integer godinaStudija, Integer semestarUGodini) {
        Predmet predmet = predmetRepo.findBySifra(sifra).orElse(null);
        if (predmet != null && programId == null) {
            throw ApiException.conflict("SUBJECT_CODE_EXISTS", "Predmet sa šifrom " + sifra + " već postoji.");
        }
        if (predmet == null) {
            predmet = new Predmet();
            predmet.setSifra(sifra);
            predmet.setNaziv(naziv);
            predmet.setOpis(opis);
            predmet.setEspb(espb);
            predmet.setObavezan(obavezan);
        }

        StudijskiProgram program = null;
        if (programId != null) {
            program = programRepo.findById(programId)
                    .orElseThrow(() -> new NoSuchElementException("Program ne postoji: " + programId));
            validatePosition(program, godinaStudija, semestarUGodini);
        }

        try {
            predmet = predmetRepo.save(predmet);
            if (program != null) linkToProgram(predmet, program, godinaStudija, semestarUGodini);
            return predmet.getId();
        } catch (DataIntegrityViolationException e) {
            throw ApiException.conflict("SUBJECT_CONFLICT", "Predmet ili veza sa programom već postoji.");
        }
    }

    private void linkToProgram(Predmet predmet, StudijskiProgram program, Integer godinaStudija, Integer semestarUGodini) {
        if (programPredmetRepo.findByProgramIdAndPredmetId(program.getId(), predmet.getId()).isPresent()) {
            throw ApiException.conflict("SUBJECT_ALREADY_ON_PROGRAM", "Predmet je već dodat na izabrani program.");
        }
        ProgramPredmet pp = new ProgramPredmet();
        pp.setProgram(program);
        pp.setPredmet(predmet);
        pp.setGodinaStudija(godinaStudija);
        pp.setSemestarUGodini(semestarUGodini);
        programPredmetRepo.save(pp);
    }

    private void validatePosition(StudijskiProgram program, Integer godinaStudija, Integer semestarUGodini) {
        if (godinaStudija == null || semestarUGodini == null) {
            throw ApiException.badRequest("Godina studija i semestar su obavezni kada se predmet dodaje na program.");
        }
        int trajanje = Optional.ofNullable(program.getTrajanjeGodina()).orElse(4);
        if (godinaStudija < 1 || godinaStudija > trajanje || semestarUGodini < 1 || semestarUGodini > 2) {
            throw ApiException.badRequest("Neispravna godina studija ili semestar.");
        }
    }

    public void update(Long id, String naziv, String opis, Integer espb) {
        Predmet p = predmetRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Predmet ne postoji: " + id));
        if (naziv != null) p.setNaziv(naziv);
        if (opis != null) p.setOpis(opis);
        if (espb != null) p.setEspb(espb);
        predmetRepo.save(p);
    }
}
