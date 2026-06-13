package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class DrziPredmetAdminService {

    private final DrziPredmetRepository drziRepo;
    private final PredmetRepository predmetRepo;
    private final NastavnikRepository nastavnikRepo;
    private final SkolskaGodinaRepository sgRepo;
    private final ProgramPredmetRepository programPredmetRepo;
    private final RealizacijaPredmetaService realizacijaService;

    public Long createOne(Long predmetId, Long nastavnikId, Long sgIdOrNull,
                          Long realizacijaId, String uloga) {
        Nastavnik nastavnik = nastavnikRepo.findById(nastavnikId)
                .orElseThrow(() -> new NoSuchElementException("Nastavnik ne postoji: " + nastavnikId));
        RealizacijaPredmeta realizacija = realizacijaId == null
                ? resolveLegacyRealization(predmetId, sgIdOrNull)
                : realizacijaService.require(realizacijaId);

        Predmet predmet = realizacija.getProgramPredmet().getPredmet();
        SkolskaGodina sg = realizacija.getSkolskaGodina();
        DrziPredmet.Uloga parsedRole;
        try {
            parsedRole = uloga == null || uloga.isBlank() ? DrziPredmet.Uloga.NOSILAC : DrziPredmet.Uloga.valueOf(uloga);
        } catch (IllegalArgumentException ex) {
            throw ApiException.badRequest("Nepoznata uloga nastavnika: " + uloga);
        }
        DrziPredmet exists = drziRepo.findFirstByRealizacijaPredmetaIdAndNastavnikIdAndUloga(
                realizacija.getId(), nastavnik.getId(), parsedRole);
        if (exists != null) return exists.getId();

        DrziPredmet dp = new DrziPredmet();
        dp.setPredmet(predmet);
        dp.setNastavnik(nastavnik);
        dp.setSkolskaGodina(sg);
        dp.setRealizacijaPredmeta(realizacija);
        dp.setUloga(parsedRole);
        return drziRepo.save(dp).getId();
    }

    private RealizacijaPredmeta resolveLegacyRealization(Long predmetId, Long sgIdOrNull) {
        if (predmetId == null) throw ApiException.badRequest("Realizacija predmeta je obavezna.");
        Predmet predmet = predmetRepo.findById(predmetId)
                .orElseThrow(() -> new NoSuchElementException("Predmet ne postoji: " + predmetId));
        SkolskaGodina sg = sgIdOrNull == null ? sgRepo.findFirstByAktivnaTrue()
                : sgRepo.findById(sgIdOrNull).orElseThrow(() -> new NoSuchElementException("Školska godina ne postoji: " + sgIdOrNull));
        if (sg == null) throw ApiException.conflict("NO_ACTIVE_SCHOOL_YEAR", "Nije podešena aktivna školska godina.");
        List<ProgramPredmet> links = programPredmetRepo.findByPredmetId(predmet.getId());
        if (links.isEmpty()) throw ApiException.conflict("SUBJECT_NOT_ON_PROGRAM", "Predmet prvo mora biti dodat na studijski program.");
        if (links.size() > 1) throw ApiException.conflict("AMBIGUOUS_SUBJECT_PROGRAM", "Predmet pripada više programa. Izaberite konkretnu realizaciju.");
        return realizacijaService.ensure(links.get(0), sg);
    }

    public void delete(Long id) {
        drziRepo.deleteById(id);
    }
}
