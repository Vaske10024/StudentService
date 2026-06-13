package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.NastavnikZvanje;
import org.raflab.studsluzba.repositories.NastavnikRepository;
import org.raflab.studsluzba.repositories.NastavnikZvanjeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class NastavnikZvanjeService {

    private final NastavnikZvanjeRepository repo;
    private final NastavnikRepository nastavnikRepo;

    public Long add(Long nastavnikId, String zvanje, String oblast, String uzaOblast, LocalDate datum, boolean aktivno) {
        Nastavnik n = nastavnikRepo.findById(nastavnikId)
                .orElseThrow(() -> new NoSuchElementException("Nastavnik ne postoji: " + nastavnikId));
        if (aktivno) {

            repo.findAll().stream()
                    .filter(x -> x.getNastavnik()!=null && n.getId().equals(x.getNastavnik().getId()))
                    .forEach(x -> { x.setAktivno(false); repo.save(x); });
        }
        NastavnikZvanje nz = new NastavnikZvanje();
        nz.setNastavnik(n);
        nz.setZvanje(zvanje);
        nz.setNaucnaOblast(oblast);
        nz.setUzaNaucnaOblast(uzaOblast);
        nz.setDatumIzbora(datum);
        nz.setAktivno(aktivno);
        return repo.save(nz).getId();
    }

    @Transactional(readOnly = true)
    public List<NastavnikZvanje> list(Long nastavnikId, boolean onlyAktivno) {
        return repo.findAll().stream()
                .filter(x -> x.getNastavnik()!=null && x.getNastavnik().getId().equals(nastavnikId))
                .filter(x -> !onlyAktivno || x.isAktivno())
                .collect(Collectors.toList());
    }

    public void activate(Long zvanjeId) {
        NastavnikZvanje target = repo.findById(zvanjeId)
                .orElseThrow(() -> new NoSuchElementException("Zvanje ne postoji: " + zvanjeId));
        // deactivate others for this nastavnik
        repo.findAll().stream()
                .filter(x -> x.getNastavnik()!=null && target.getNastavnik()!=null
                        && x.getNastavnik().getId().equals(target.getNastavnik().getId()))
                .forEach(x -> { x.setAktivno(false); repo.save(x); });
        target.setAktivno(true);
        repo.save(target);
    }
}
