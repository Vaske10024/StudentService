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
import org.raflab.studsluzba.repositories.SlusaPredmetRepository;
import org.raflab.studsluzba.repositories.DrziPredmetRepository;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.dtos.PredispitGradebookDTO;
import org.raflab.studsluzba.model.dtos.PredispitStudentScoreDTO;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Transactional
public class OstvarenaPredObavService {

    private final OstvarenaPredObavRepository repo;
    private final PredispitnaObavezaRepository poRepo;
    private final StudentIndeksRepository indeksRepo;
    private final IspitRepository ispitRepo;
    private final SlusaPredmetRepository slusaRepo;
    private final DrziPredmetRepository drziRepo;

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
        if (predmetId == null || sgId == null || !slusaRepo.existsStudentSlusaPredmetUGodini(studentIndeksId, predmetId, sgId)) {
            throw ApiException.conflict("STUDENT_NOT_ENROLLED_IN_SUBJECT",
                    "Student ne sluša predmet ove predispitne obaveze u izabranoj školskoj godini.");
        }
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

    @Transactional(readOnly = true)
    public List<PredispitGradebookDTO> gradebook(Long drziPredmetId) {
        DrziPredmet assignment = drziRepo.findById(drziPredmetId)
                .orElseThrow(() -> ApiException.notFound("Nastavna dodela ne postoji: " + drziPredmetId));
        if (assignment.getPredmet() == null || assignment.getSkolskaGodina() == null) {
            throw ApiException.conflict("ASSIGNMENT_INCOMPLETE", "Nastavna dodela nema predmet ili školsku godinu.");
        }
        List<StudentIndeks> students = slusaRepo.getStudentiSlusaPredmetZaDrziPredmet(drziPredmetId);
        return poRepo.findByPredmetIdAndSkolskaGodinaId(assignment.getPredmet().getId(), assignment.getSkolskaGodina().getId())
                .stream().map(definition -> {
                    Map<Long, OstvarenaPredObav> scores = repo.findByObavezaId(definition.getId()).stream()
                            .collect(Collectors.toMap(item -> item.getStudent().getId(), Function.identity()));
                    List<PredispitStudentScoreDTO> rows = students.stream().map(student -> {
                        OstvarenaPredObav score = scores.get(student.getId());
                        return new PredispitStudentScoreDTO(
                                student.getId(),
                                student.getStudent() == null ? null : student.getStudent().getId(),
                                student.getStudent() == null ? null : student.getStudent().getIme(),
                                student.getStudent() == null ? null : student.getStudent().getPrezime(),
                                student.getStudProgramOznaka(),
                                student.getBroj(),
                                student.getGodina(),
                                score == null ? 0 : score.getOsvojeniPoeni()
                        );
                    }).collect(Collectors.toList());
                    return new PredispitGradebookDTO(definition.getId(), definition.getVrsta(), definition.getMaxPoeni(), rows);
                }).collect(Collectors.toList());
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
