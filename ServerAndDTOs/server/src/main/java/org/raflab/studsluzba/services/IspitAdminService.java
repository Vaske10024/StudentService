package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.ispiti.PrijavaStatus;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.IspitniRokRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class IspitAdminService {

    private final IspitRepository ispitRepository;
    private final IspitniRokRepository rokRepository;
    private final DrziPredmetRepository drziPredmetRepository;
    private final IspitQueryRepository prijavaRepository;
    private final AcademicProgressService academicProgressService;
    private final NotificationService notificationService;

    @PersistenceContext
    private EntityManager em;

    @Transactional
    public Long create(Long rokId, Long drziPredmetId, LocalDate datum, LocalTime vreme) {
        return create(rokId, drziPredmetId, datum, vreme, null, null, null);
    }

    @Transactional
    public Long create(Long rokId, Long drziPredmetId, LocalDate datum, LocalTime vreme,
                       LocalDateTime registrationStart, LocalDateTime registrationEnd, LocalDateTime cancellationEnd) {
        if (rokId == null) throw new IllegalArgumentException("rokId je obavezan.");
        if (drziPredmetId == null) throw new IllegalArgumentException("drziPredmetId je obavezan.");
        if (datum == null) throw new IllegalArgumentException("datum je obavezan.");
        if (vreme == null) throw new IllegalArgumentException("vreme je obavezno.");

        DrziPredmet dp = drziPredmetRepository.findById(drziPredmetId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeci drzi-predmet id=" + drziPredmetId));
        IspitniRok rok = rokRepository.findById(rokId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeci ispitni rok id=" + rokId));
        validateExamDateWithinPeriod(rok, datum);

        if (dp.getNastavnik() == null || dp.getNastavnik().getId() == null) {
            throw new IllegalStateException("DrziPredmet id=" + drziPredmetId + " nema nastavnika.");
        }
        if (dp.getPredmet() == null || dp.getPredmet().getId() == null) {
            throw new IllegalStateException("DrziPredmet id=" + drziPredmetId + " nema predmet.");
        }

        Ispit ispit = new Ispit();
        ispit.setIspitniRok(rok);
        ispit.setDrziPredmet(dp);
        ispit.setNastavnik(dp.getNastavnik());
        ispit.setPredmet(dp.getPredmet());
        ispit.setDatumOdrzavanja(datum);
        ispit.setVremePocetka(vreme);
        applyWindows(ispit, rok, registrationStart, registrationEnd, cancellationEnd);
        ispit.setZakljucen(false);

        log.info("Kreiranje ispita: rokId={}, drziPredmetId={}, nastavnikId={}, predmetId={}, datum={}, vreme={}",
                rokId, drziPredmetId, dp.getNastavnik().getId(), dp.getPredmet().getId(), datum, vreme);

        Ispit saved = ispitRepository.save(ispit);
        em.flush();
        return saved.getId();
    }

    @Transactional
    public void updateTime(Long ispitId, LocalDate datum, LocalTime vreme) {
        updateTime(ispitId, datum, vreme, null, null, null);
    }

    @Transactional
    public void updateTime(Long ispitId, LocalDate datum, LocalTime vreme,
                           LocalDateTime registrationStart, LocalDateTime registrationEnd, LocalDateTime cancellationEnd) {
        if (ispitId == null) throw new IllegalArgumentException("ispitId je obavezan.");

        Ispit ispit = ispitRepository.findById(ispitId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeci ispit id=" + ispitId));

        if (ispit.isZakljucen()) {
            throw new IllegalStateException("Ispit je zakljucan i vreme se ne moze menjati.");
        }

        if (datum != null) {
            validateExamDateWithinPeriod(ispit.getIspitniRok(), datum);
            ispit.setDatumOdrzavanja(datum);
        }
        if (vreme != null) ispit.setVremePocetka(vreme);
        if (registrationStart != null || registrationEnd != null || cancellationEnd != null) {
            applyWindows(ispit, ispit.getIspitniRok(), registrationStart, registrationEnd, cancellationEnd);
        }

        ispitRepository.save(ispit);
        em.flush();
    }

    @Transactional
    public void lock(Long ispitId) {
        if (ispitId == null) throw new IllegalArgumentException("ispitId je obavezan.");

        Ispit ispit = ispitRepository.findById(ispitId)
                .orElseThrow(() -> new IllegalArgumentException("Nepostojeci ispit id=" + ispitId));

        if (!ispit.isZakljucen()) {
            for (PrijavaIspita prijava : prijavaRepository.findByIspitId(ispitId)) {
                if (Boolean.TRUE.equals(prijava.getPonisteno()) || prijava.getStatus() == PrijavaStatus.ODJAVLJEN) continue;
                if (!prijava.isDaLiJeIzasao()) {
                    prijava.setStatus(PrijavaStatus.ODSUTAN);
                } else if (prijava.getOcena() >= 6) {
                    prijava.setStatus(PrijavaStatus.POLOZIO);
                } else {
                    prijava.setStatus(PrijavaStatus.PAO);
                }
                prijavaRepository.save(prijava);
            }
            ispit.setZakljucen(true);
            ispitRepository.save(ispit);
            em.flush();
            prijavaRepository.findByIspitId(ispitId).stream()
                    .filter(p -> p.getStudent() != null && p.getStudent().getId() != null)
                    .map(p -> p.getStudent().getId()).distinct()
                    .forEach(indeksId -> {
                        academicProgressService.recalculateEarnedEcts(indeksId);
                        notificationService.notifyStudent(indeksId, "EXAM_RESULTS_LOCKED",
                                "Rezultati ispita su zakljucani", "Zakljucani su rezultati ispita #" + ispitId + ".");
                    });
        }
    }

    private void validateExamDateWithinPeriod(IspitniRok rok, LocalDate datum) {
        if (rok != null && datum != null && ((rok.getDatumPocetka() != null && datum.isBefore(rok.getDatumPocetka()))
                || (rok.getDatumZavrsetka() != null && datum.isAfter(rok.getDatumZavrsetka())))) {
            throw new IllegalArgumentException("Datum ispita mora biti unutar izabranog ispitnog roka.");
        }
    }

    private void applyWindows(Ispit ispit, IspitniRok rok, LocalDateTime registrationStart,
                              LocalDateTime registrationEnd, LocalDateTime cancellationEnd) {
        LocalDateTime start = registrationStart != null ? registrationStart : ispit.getRegistrationStart();
        LocalDateTime end = registrationEnd != null ? registrationEnd : ispit.getRegistrationEnd();
        LocalDateTime cancel = cancellationEnd != null ? cancellationEnd : ispit.getCancellationEnd();
        if (start == null && rok != null) start = rok.getRegistrationStart();
        if (end == null && rok != null) end = rok.getRegistrationEnd();
        if (cancel == null && rok != null) cancel = rok.getCancellationEnd();
        validateWindows(start, end, cancel);
        ispit.setRegistrationStart(start);
        ispit.setRegistrationEnd(end);
        ispit.setCancellationEnd(cancel);
    }

    private void validateWindows(LocalDateTime registrationStart, LocalDateTime registrationEnd,
                                 LocalDateTime cancellationEnd) {
        if (registrationStart == null || registrationEnd == null || cancellationEnd == null) {
            throw new IllegalArgumentException("Prozori prijave i odjave su obavezni za ispit.");
        }
        if (registrationEnd.isBefore(registrationStart)) {
            throw new IllegalArgumentException("Kraj prijave mora biti posle pocetka prijave.");
        }
        if (cancellationEnd.isBefore(registrationStart)) {
            throw new IllegalArgumentException("Kraj odjave mora biti posle pocetka prijave.");
        }
    }
}
