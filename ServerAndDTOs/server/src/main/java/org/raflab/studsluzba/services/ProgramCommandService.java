package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ProgramPredmet;

import org.raflab.studsluzba.model.dtos.ProgramPredmetCreateRequest;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.raflab.studsluzba.repositories.ProgramPredmetRepository;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class ProgramCommandService {

    private final PredmetRepository predmetRepo;
    private final ProgramPredmetRepository ppRepo;
    private final StudijskiProgramRepository spRepo;

    public Long addPredmetNaProgram(Long programId, ProgramPredmetCreateRequest req) {
        StudijskiProgram program = spRepo.findById(programId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeći program: " + programId));

        // 1) Predmet (kao i pre)
        Predmet predmet = ((java.util.List<Predmet>) predmetRepo.findAll())
                .stream()
                .filter(p -> req.getSifra().equalsIgnoreCase(p.getSifra()))
                .findFirst()
                .orElse(null);

        if (predmet == null) {
            predmet = new Predmet();
            predmet.setSifra(req.getSifra());
            predmet.setNaziv(req.getNaziv());
            predmet.setOpis(req.getOpis());
            predmet.setEspb(req.getEspb());
            predmet.setStudProgram(program);
            predmet.setObavezan(true);
            try {
                predmet = predmetRepo.save(predmet);
            } catch (DataIntegrityViolationException ex) {
                throw new IllegalStateException("Duplikat šifre predmeta: " + req.getSifra(), ex);
            }
        }

        // 2) Odredi godinu+semestar u godini
        int godinaStudija;
        int semestarUGodini;

        if (req.getGodinaStudija() != null && req.getSemestarUGodini() != null) {
            godinaStudija = req.getGodinaStudija();
            semestarUGodini = req.getSemestarUGodini();
        } else if (req.getSemestar() != null) {
            int s = req.getSemestar(); // 1..8
            godinaStudija = (s + 1) / 2;           // 1..4
            semestarUGodini = (s % 2 == 0) ? 2 : 1; // 1..2
        } else {
            throw new IllegalArgumentException("Moraš poslati (godinaStudija + semestarUGodini) ili stari semestar(1..8).");
        }

        // 3) Validacija u odnosu na program (ako trajanjeGodina nije setovano, tretiramo kao 4)
        int trajanje = Optional.ofNullable(program.getTrajanjeGodina()).orElse(4);
        if (godinaStudija < 1 || godinaStudija > trajanje) {
            throw new IllegalArgumentException("godinaStudija mora biti u opsegu [1.." + trajanje + "].");
        }
        if (semestarUGodini < 1 || semestarUGodini > 2) {
            throw new IllegalArgumentException("semestarUGodini mora biti 1 ili 2.");
        }

        // 4) Ako već postoji veza program+predmet, vrati postojeći id (umesto da puca unique constraint)
        ppRepo.findByProgramIdAndPredmetId(program.getId(), predmet.getId())
                .ifPresent(existing -> { throw new IllegalStateException("Predmet je već dodat na program (PP#" + existing.getId() + ")."); });

        ProgramPredmet pp = new ProgramPredmet();
        pp.setProgram(program);
        pp.setPredmet(predmet);
        pp.setGodinaStudija(godinaStudija);
        pp.setSemestarUGodini(semestarUGodini);
        pp.setFondPredavanja(req.getFondPredavanja());
        pp.setFondVezbi(req.getFondVezbi());
        pp.setFondPraktikum(req.getFondPraktikum());

        return ppRepo.save(pp).getId();
    }
}
