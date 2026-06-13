package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.SrednjaSkola;
import org.raflab.studsluzba.repositories.SrednjaSkolaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class SrednjaSkolaService {
    private final SrednjaSkolaRepository repo;

    public Long create(String naziv, String mesto, String vrsta) {
        SrednjaSkola s = repo.findByNaziv(naziv);
        if (s != null) return s.getId();
        s = new SrednjaSkola();
        s.setNaziv(naziv); s.setMesto(mesto); s.setVrsta(vrsta);
        return repo.save(s).getId();
    }

    @Transactional(readOnly = true)
    public List<SrednjaSkola> all() { return repo.findAll(); }
}
