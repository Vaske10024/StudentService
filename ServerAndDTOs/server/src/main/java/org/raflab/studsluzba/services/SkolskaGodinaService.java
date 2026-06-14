package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class SkolskaGodinaService {

    private final SkolskaGodinaRepository repo;

    public Long create(String oznaka, boolean aktivna) {
        repo.lockAllForActivation();
        SkolskaGodina sg = new SkolskaGodina();
        sg.setGodina(oznaka);
        if (aktivna) {
            repo.deactivateAllActive();
        }
        sg.setAktivna(aktivna);
        return repo.save(sg).getId();
    }

    public void activate(Long id) {
        repo.lockAllForActivation();
        SkolskaGodina target = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ne postoji školska godina id=" + id));
        repo.deactivateAllActive();
        SkolskaGodina managedTarget = repo.findById(target.getId())
                .orElseThrow(() -> new NoSuchElementException("Ne postoji skolska godina id=" + id));
        managedTarget.setAktivna(true);
        repo.save(managedTarget);
    }

    @Transactional(readOnly = true)
    public Iterable<SkolskaGodina> all() {
        return repo.findAll();
    }

    @Transactional(readOnly = true)
    public SkolskaGodina active() {
        List<SkolskaGodina> active = repo.findAllByAktivnaTrue();
        if (active.isEmpty()) throw new IllegalStateException("Nije definisana aktivna školska godina.");
        if (active.size() > 1) throw new IllegalStateException("Postoji više aktivnih školskih godina; kontaktirajte administratora.");
        return active.get(0);
    }
}
