package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.dtos.NastavnikLiteDTO;
import org.raflab.studsluzba.repositories.NastavnikRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NastavnikService {

    private final NastavnikRepository nastavnikRepository;

    public Nastavnik save(Nastavnik nastavnik) {
        nastavnik.setEmail(clean(nastavnik.getEmail()));
        nastavnik.setJmbg(clean(nastavnik.getJmbg()));
        if (nastavnik.getEmail() != null && nastavnikRepository.existsByEmailIgnoreCase(nastavnik.getEmail())) {
            throw ApiException.conflict("PROFESSOR_EMAIL_EXISTS", "Profesor sa ovim email-om već postoji.");
        }
        if (nastavnik.getJmbg() != null && nastavnikRepository.existsByJmbg(nastavnik.getJmbg())) {
            throw ApiException.conflict("PROFESSOR_JMBG_EXISTS", "Profesor sa ovim JMBG-om već postoji.");
        }
        return nastavnikRepository.save(nastavnik);
    }

    private String clean(String value) {
        if (value == null) return null;
        String cleaned = value.trim();
        return cleaned.isEmpty() ? null : cleaned;
    }

    public Iterable<Nastavnik> findAll() {
        return nastavnikRepository.findAll();
    }

    public Optional<Nastavnik> findById(Long id) {
        return nastavnikRepository.findById(id);
    }

    public List<Nastavnik> findByImeAndPrezime(String ime, String prezime) {
        return nastavnikRepository.findByImeAndPrezime(ime, prezime);
    }


    @Transactional
    public List<NastavnikLiteDTO> findAllLite() { return nastavnikRepository.findAllLite(); }

    @Transactional
    public List<NastavnikLiteDTO> searchLite(String ime, String prezime) { return nastavnikRepository.searchLite(ime, prezime); }

}
