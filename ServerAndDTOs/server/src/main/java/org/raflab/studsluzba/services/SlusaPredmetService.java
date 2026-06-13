package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.UpisGodine;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.repositories.SlusaPredmetRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.UpisGodineRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class SlusaPredmetService {

    private final SlusaPredmetRepository slusaRepo;
    private final DrziPredmetRepository drziRepo;
    private final StudentIndeksRepository indeksRepo;
    private final UpisGodineRepository upisRepo;

    public Long add(Long indeksId, Long drziPredmetId) {
        StudentIndeks indeks = indeksRepo.findById(indeksId)
                .orElseThrow(() -> new NoSuchElementException("Indeks ne postoji: " + indeksId));
        DrziPredmet dp = drziRepo.findById(drziPredmetId)
                .orElseThrow(() -> new NoSuchElementException("DrziPredmet ne postoji: " + drziPredmetId));
        if (dp.getRealizacijaPredmeta() == null) {
            throw ApiException.conflict("ASSIGNMENT_WITHOUT_REALIZATION", "Angažovanje nije vezano za realizaciju predmeta.");
        }
        List<UpisGodine> upisi = upisRepo.findUpisi(indeksId);
        if (upisi.isEmpty()) throw ApiException.conflict("INDEX_NOT_ENROLLED", "Student prvo mora upisati godinu studija.");
        return enroll(indeks, dp.getRealizacijaPredmeta(), upisi.get(0));
    }

    public void enrollRealizations(StudentIndeks indeks, UpisGodine upis, List<RealizacijaPredmeta> realizacije) {
        for (RealizacijaPredmeta realizacija : realizacije) enroll(indeks, realizacija, upis);
    }

    public Long enroll(StudentIndeks indeks, RealizacijaPredmeta realizacija, UpisGodine upis) {
        if (slusaRepo.existsByStudentIndeksIdAndRealizacijaPredmetaId(indeks.getId(), realizacija.getId())) {
            return slusaRepo.getSlusaPredmetForIndeksAktivnaGodina(indeks.getId()).stream()
                    .filter(s -> s.getRealizacijaPredmeta().getId().equals(realizacija.getId()))
                    .map(SlusaPredmet::getId).findFirst().orElse(null);
        }
        SlusaPredmet slusa = new SlusaPredmet();
        slusa.setStudentIndeks(indeks);
        slusa.setRealizacijaPredmeta(realizacija);
        slusa.setUpisGodine(upis);
        slusa.setSkolskaGodina(realizacija.getSkolskaGodina());
        return slusaRepo.save(slusa).getId();
    }

    public void remove(Long slusaId) {
        slusaRepo.deleteById(slusaId);
    }

    @Transactional(readOnly = true)
    public List<SlusaPredmet> byIndeks(Long indeksId) {
        return slusaRepo.getSlusaPredmetForIndeksAktivnaGodina(indeksId);
    }
}
