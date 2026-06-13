package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.VisokaSkola;
import org.raflab.studsluzba.repositories.VisokaSkolaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service @RequiredArgsConstructor @Transactional
public class VisokaSkolaService {
    private final VisokaSkolaRepository repo;

    public Long create(String naziv, String mesto, String tip) {
        VisokaSkola v = repo.findByNaziv(naziv);
        if (v != null) return v.getId();
        v = new VisokaSkola();
        v.setNaziv(naziv); v.setMesto(mesto); v.setTip(tip);
        return repo.save(v).getId();
    }

    @Transactional(readOnly = true)
    public List<VisokaSkola> all() { return repo.findAll(); }
}
