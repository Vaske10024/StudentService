package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ispiti.PredispitnaObaveza;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.PredispitnaObavezaRepository;
import org.raflab.studsluzba.repositories.PredmetRepository;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class PredispitnaObavezaService {

    private final PredispitnaObavezaRepository repo;
    private final PredmetRepository predmetRepo;
    private final SkolskaGodinaRepository sgRepo;
    private final IspitRepository ispitRepo;
    private final GradingService gradingService;

    public Long create(Long predmetId, Long sgId, String vrsta, int maxPoeni) {
        validateMax(maxPoeni);
        ensureNoLockedExam(predmetId, sgId);
        Predmet p = predmetRepo.findById(predmetId).orElseThrow(() -> new NoSuchElementException("Predmet ne postoji: " + predmetId));
        SkolskaGodina sg = sgRepo.findById(sgId).orElseThrow(() -> new NoSuchElementException("SG ne postoji: " + sgId));
        ensurePredispitTotalAllowed(predmetId, sgId, null, maxPoeni);

        PredispitnaObaveza po = new PredispitnaObaveza();
        po.setPredmet(p);
        po.setSkolskaGodina(sg);
        po.setVrsta(vrsta);
        po.setMaxPoeni(maxPoeni);
        return repo.save(po).getId();
    }

    public void updateMax(Long id, int maxPoeni) {
        validateMax(maxPoeni);
        PredispitnaObaveza po = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Predispitna ne postoji: " + id));
        Long predmetId = po.getPredmet() == null ? null : po.getPredmet().getId();
        Long sgId = po.getSkolskaGodina() == null ? null : po.getSkolskaGodina().getId();
        ensureNoLockedExam(predmetId, sgId);
        ensurePredispitTotalAllowed(predmetId, sgId, id, maxPoeni);
        po.setMaxPoeni(maxPoeni);
        repo.save(po);
    }

    public void delete(Long id) {
        PredispitnaObaveza po = repo.findById(id).orElseThrow(() -> new NoSuchElementException("Predispitna ne postoji: " + id));
        Long predmetId = po.getPredmet() == null ? null : po.getPredmet().getId();
        Long sgId = po.getSkolskaGodina() == null ? null : po.getSkolskaGodina().getId();
        ensureNoLockedExam(predmetId, sgId);
        repo.delete(po);
    }

    @Transactional(readOnly = true)
    public List<PredispitnaObaveza> list(Long predmetId, Long sgId) {
        return repo.findByPredmetIdAndSkolskaGodinaId(predmetId, sgId);
    }

    private void validateMax(int maxPoeni) {
        if (maxPoeni < 0 || maxPoeni > gradingService.getPredispitMaxTotalPoints()) {
            throw ApiException.badRequest("Maksimalni predispitni poeni moraju biti u opsegu [0.." + gradingService.getPredispitMaxTotalPoints() + "].");
        }
    }

    private void ensurePredispitTotalAllowed(Long predmetId, Long sgId, Long replacingId, int newMax) {
        int existingTotal = repo.findByPredmetIdAndSkolskaGodinaId(predmetId, sgId).stream()
                .filter(po -> replacingId == null || !replacingId.equals(po.getId()))
                .mapToInt(PredispitnaObaveza::getMaxPoeni)
                .sum();
        int total = existingTotal + newMax;
        if (total > gradingService.getPredispitMaxTotalPoints()) {
            throw ApiException.conflict("Ukupan maksimum predispitnih obaveza ne sme preći " + gradingService.getPredispitMaxTotalPoints() + " poena.");
        }
    }

    private void ensureNoLockedExam(Long predmetId, Long sgId) {
        if (predmetId != null && sgId != null && ispitRepo.existsLockedForPredmetAndSkolskaGodina(predmetId, sgId)) {
            throw ApiException.conflict("Postoji zaključan ispit za predmet i školsku godinu; definicija predispitnih obaveza se ne može menjati.");
        }
    }
}
