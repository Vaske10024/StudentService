package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ProgramPredmet;
import org.raflab.studsluzba.model.ispiti.RealizacijaPredmeta;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.repositories.ProgramPredmetRepository;
import org.raflab.studsluzba.repositories.RealizacijaPredmetaRepository;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RealizacijaPredmetaService {

    private final RealizacijaPredmetaRepository realizacijaRepo;
    private final ProgramPredmetRepository programPredmetRepo;
    private final SkolskaGodinaRepository skolskaGodinaRepo;

    public List<RealizacijaPredmeta> generateForProgram(Long programId, Long skolskaGodinaId) {
        SkolskaGodina sg = requireSchoolYear(skolskaGodinaId);
        List<RealizacijaPredmeta> result = new ArrayList<>();
        for (ProgramPredmet pp : programPredmetRepo.findProgramPredmeti(programId)) {
            result.add(ensure(pp, sg));
        }
        return result;
    }

    public List<RealizacijaPredmeta> ensureForEnrollment(Long programId, int godinaStudija) {
        return ensureForEnrollment(programId, godinaStudija, activeSchoolYear());
    }

    public List<RealizacijaPredmeta> ensureForEnrollment(Long programId, int godinaStudija, Long skolskaGodinaId) {
        return ensureForEnrollment(programId, godinaStudija, requireSchoolYear(skolskaGodinaId));
    }

    private List<RealizacijaPredmeta> ensureForEnrollment(Long programId, int godinaStudija, SkolskaGodina sg) {
        List<ProgramPredmet> plan = programPredmetRepo
                .findByProgramIdAndGodinaStudijaOrderBySemestarUGodini(programId, godinaStudija);
        if (plan.isEmpty()) {
            throw ApiException.conflict("PROGRAM_YEAR_HAS_NO_SUBJECTS",
                    "Studijski program nema definisane predmete za " + godinaStudija + ". godinu.");
        }
        for (ProgramPredmet pp : plan) ensure(pp, sg);
        return realizacijaRepo.findForEnrollment(programId, godinaStudija, sg.getId());
    }

    public RealizacijaPredmeta ensure(ProgramPredmet pp, SkolskaGodina sg) {
        return realizacijaRepo.findByProgramPredmetIdAndSkolskaGodinaId(pp.getId(), sg.getId())
                .orElseGet(() -> {
                    RealizacijaPredmeta r = new RealizacijaPredmeta();
                    r.setProgramPredmet(pp);
                    r.setSkolskaGodina(sg);
                    r.setStatus(RealizacijaPredmeta.Status.ACTIVE);
                    return realizacijaRepo.save(r);
                });
    }

    @Transactional(readOnly = true)
    public List<RealizacijaPredmeta> all(Long skolskaGodinaId) {
        SkolskaGodina sg = requireSchoolYear(skolskaGodinaId);
        return realizacijaRepo.findAllBySkolskaGodinaIdOrderById(sg.getId());
    }

    @Transactional(readOnly = true)
    public RealizacijaPredmeta require(Long id) {
        return realizacijaRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Realizacija predmeta ne postoji: " + id));
    }

    private SkolskaGodina requireSchoolYear(Long id) {
        if (id == null) return activeSchoolYear();
        return skolskaGodinaRepo.findById(id)
                .orElseThrow(() -> ApiException.notFound("Školska godina ne postoji: " + id));
    }

    private SkolskaGodina activeSchoolYear() {
        SkolskaGodina sg = skolskaGodinaRepo.findFirstByAktivnaTrue();
        if (sg == null) throw ApiException.conflict("NO_ACTIVE_SCHOOL_YEAR", "Nije podešena aktivna školska godina.");
        return sg;
    }
}
