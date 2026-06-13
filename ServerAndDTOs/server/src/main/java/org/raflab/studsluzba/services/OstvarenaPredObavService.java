package org.raflab.studsluzba.services;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.ispiti.OstvarenaPredObav;
import org.raflab.studsluzba.model.ispiti.PredispitnaObaveza;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.OstvarenaPredObavRepository;
import org.raflab.studsluzba.repositories.PredispitnaObavezaRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OstvarenaPredObavService {

    private final OstvarenaPredObavRepository repo;
    private final PredispitnaObavezaRepository poRepo;
    private final StudentIndeksRepository indeksRepo;
    private final IspitRepository ispitRepo;

    @Transactional(readOnly = true)
    public int ukupniPoeni(Long studentIndeksId, Long predmetId, Long skolskaGodinaId) {
        Integer sum = repo.ostvareniPredispitniPoeni(studentIndeksId, predmetId, skolskaGodinaId);
        return sum == null ? 0 : sum;
    }

    public Long upsert(Long studentIndeksId, Long predObId, int poeni) {
        StudentIndeks si = indeksRepo.findById(studentIndeksId)
                .orElseThrow(() -> new NoSuchElementException("Indeks ne postoji: " + studentIndeksId));
        PredispitnaObaveza po = poRepo.findById(predObId)
                .orElseThrow(() -> new NoSuchElementException("Predispitna ne postoji: " + predObId));

        if (poeni < 0 || poeni > po.getMaxPoeni()) {
            throw ApiException.badRequest("Ostvareni predispitni poeni moraju biti u opsegu [0.." + po.getMaxPoeni() + "].");
        }
        Long predmetId = po.getPredmet() == null ? null : po.getPredmet().getId();
        Long sgId = po.getSkolskaGodina() == null ? null : po.getSkolskaGodina().getId();
        if (predmetId != null && sgId != null && ispitRepo.existsLockedForPredmetAndSkolskaGodina(predmetId, sgId)) {
            throw ApiException.conflict("Postoji zaključan ispit; predispitni poeni se ne mogu menjati normalnim tokom.");
        }

        OstvarenaPredObav x = repo.findByStudentAndObaveza(si.getId(), po.getId()).orElse(null);
        if (x == null) {
            x = new OstvarenaPredObav();
            x.setStudent(si);
            x.setObaveza(po);
        }
        x.setOsvojeniPoeni(poeni);
        return repo.save(x).getId();
    }

    @Transactional(readOnly = true)
    public List<DetaljDTO> listDetalji(Long studentIndeksId, Long predmetId, Long sgId) {
        List<PredispitnaObaveza> definicije = poRepo.findByPredmetIdAndSkolskaGodinaId(predmetId, sgId);
        return definicije.stream()
                .map(def -> {
                    int osvojeni = repo.findByStudentAndObaveza(studentIndeksId, def.getId())
                            .map(OstvarenaPredObav::getOsvojeniPoeni).orElse(0);
                    return new DetaljDTO(def.getId(), def.getVrsta(), def.getMaxPoeni(), osvojeni);
                })
                .collect(Collectors.toList());
    }

    @Data
    @AllArgsConstructor
    public static class DetaljDTO {
        private Long predObId;
        private String vrsta;
        private Integer max;
        private Integer osvojeni;
    }
}
