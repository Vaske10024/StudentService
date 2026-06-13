package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.IspitniRokRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IspitAdminService {

    private final IspitRepository ispitRepository;
    private final IspitniRokRepository rokRepository;
    private final DrziPredmetRepository drziPredmetRepository;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Long create(Long rokId, Long drziPredmetId, LocalDate datum, LocalTime vreme) {
        if (rokId == null) throw new IllegalArgumentException("rokId je obavezan.");
        if (drziPredmetId == null) throw new IllegalArgumentException("drziPredmetId je obavezan.");
        if (datum == null) throw new IllegalArgumentException("datum je obavezan.");
        if (vreme == null) throw new IllegalArgumentException("vreme je obavezno.");

        // Učitaj dp da bismo izvukli nastavnik_id i predmet_id (baza traži oba NOT NULL u ISPIT)
        DrziPredmet dp = drziPredmetRepository.findById(drziPredmetId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeći drži-predmet id=" + drziPredmetId));

        if (!rokRepository.existsById(rokId)) {
            throw new IllegalArgumentException("Nepostojeći ispitni rok id=" + rokId);
        }
        IspitniRok rokRef = em.getReference(IspitniRok.class, rokId);

        if (dp.getNastavnik() == null || dp.getNastavnik().getId() == null) {
            throw new IllegalStateException("DržiPredmet id=" + drziPredmetId + " nema nastavnika (nastavnik_id je obavezan).");
        }
        if (dp.getPredmet() == null || dp.getPredmet().getId() == null) {
            throw new IllegalStateException("DržiPredmet id=" + drziPredmetId + " nema predmet (predmet_id je obavezan).");
        }

        Ispit ispit = new Ispit();
        ispit.setIspitniRok(rokRef);
        ispit.setDrziPredmet(dp);

        // ✅ obavezne kolone u tabeli ISPIT
        ispit.setNastavnik(dp.getNastavnik());
        ispit.setPredmet(dp.getPredmet());

        ispit.setDatumOdrzavanja(datum);
        ispit.setVremePocetka(vreme);
        ispit.setZakljucen(false);

        log.info("Kreiranje ispita: rokId={}, drziPredmetId={}, nastavnikId={}, predmetId={}, datum={}, vreme={}",
                rokId, drziPredmetId, dp.getNastavnik().getId(), dp.getPredmet().getId(), datum, vreme);

        Ispit saved = ispitRepository.save(ispit);
        em.flush();
        return saved.getId();
    }

    @Transactional
    public void updateTime(Long ispitId, LocalDate datum, LocalTime vreme) {
        if (ispitId == null) throw new IllegalArgumentException("ispitId je obavezan.");

        Ispit ispit = ispitRepository.findById(ispitId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeći ispit id=" + ispitId));

        if (ispit.isZakljucen()) {
            throw new IllegalStateException("Ispit je zaključan i vreme se ne može menjati.");
        }

        if (datum != null) ispit.setDatumOdrzavanja(datum);
        if (vreme != null) ispit.setVremePocetka(vreme);

        ispitRepository.save(ispit);
        em.flush();
    }

    @Transactional
    public void lock(Long ispitId) {
        if (ispitId == null) throw new IllegalArgumentException("ispitId je obavezan.");

        Ispit ispit = ispitRepository.findById(ispitId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeći ispit id=" + ispitId));

        if (!ispit.isZakljucen()) {
            ispit.setZakljucen(true);
            ispitRepository.save(ispit);
            em.flush();
        }
    }
}
