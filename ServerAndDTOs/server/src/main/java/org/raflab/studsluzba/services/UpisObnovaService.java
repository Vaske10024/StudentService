package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.*;
import org.raflab.studsluzba.model.dtos.ObnovaCreateRequest;
import org.raflab.studsluzba.model.dtos.UpisCreateRequest;
import org.raflab.studsluzba.model.dtos.UpisanaGodinaDTO;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.repositories.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UpisObnovaService {

    private final StudentIndeksRepository indeksRepo;
    private final DrziPredmetRepository drziPredRepo;
    private final SlusaPredmetRepository slusaRepo;
    private final UpisGodineRepository upisRepo;
    private final ObnovaGodineRepository obnovaRepo;
    private final SkolskaGodinaRepository sgRepo;
    private final PredmetRepository predmetRepo;

    // NOVO
    private final ProgramPredmetRepository ppRepo;
    private final ECTSRuleService ectsRuleService;
    private final RealizacijaPredmetaService realizacijaService;
    private final SlusaPredmetService slusaPredmetService;

    @Transactional(readOnly = true)
    public List<UpisGodine> readUpisi(Long indeksId) {
        return upisRepo.findUpisi(indeksId);
    }

    @Transactional(readOnly = true)
    public List<ObnovaGodine> readObnove(Long indeksId) {
        return obnovaRepo.findObnove(indeksId);
    }
    @Transactional(readOnly = true)
    public List<UpisanaGodinaDTO> readObnoveDto(Long indeksId) {
        List<ObnovaGodine> obnove = obnovaRepo.findObnove(indeksId);
        return obnove.stream()
                .map(o -> new UpisanaGodinaDTO(
                        o.getId(),
                        "OBNOVA",
                        o.getObnavljaGodinu(),
                        o.getDatum(),
                        o.getNapomena()
                ))
                .sorted(Comparator.comparing(UpisanaGodinaDTO::getDatum))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UpisanaGodinaDTO> pregledZaBrojIndeksa(String sp, int godina, int broj) {
        StudentIndeks si = indeksRepo.findStudentIndeks(sp, godina, broj);
        if (si == null) {
            throw new NoSuchElementException("Ne postoji indeks: " + sp + " " + godina + " " + broj);
        }

        Long indeksId = si.getId();
        List<UpisanaGodinaDTO> result = new ArrayList<>();

        for (UpisGodine u : upisRepo.findUpisi(indeksId)) {
            result.add(new UpisanaGodinaDTO(
                    u.getId(),
                    "UPIS",
                    u.getUpisujeGodinu(),
                    u.getDatum(),
                    u.getNapomena()
            ));
        }

        for (ObnovaGodine o : obnovaRepo.findObnove(indeksId)) {
            result.add(new UpisanaGodinaDTO(
                    o.getId(),
                    "OBNOVA",
                    o.getObnavljaGodinu(),
                    o.getDatum(),
                    o.getNapomena()
            ));
        }

        result.sort(Comparator.comparing(UpisanaGodinaDTO::getDatum));
        return result;
    }

    @Transactional
    public Long upisi(UpisCreateRequest req) {
        StudentIndeks si = indeksRepo.findById(req.getIndeksId())
                .orElseThrow(() -> new NoSuchElementException("Ne postoji indeks: " + req.getIndeksId()));

        int upisujeGodinu = (req.getUpisujeGodinu() != null)
                ? req.getUpisujeGodinu()
                : odrediGodinuStudija(si.getId());

        int maxGodina = programTrajanje(si);
        if (upisujeGodinu < 1 || upisujeGodinu > maxGodina) {
            throw new IllegalArgumentException("upisujeGodinu mora biti u opsegu [1.." + maxGodina + "].");
        }
        ectsRuleService.assertEnrollmentAllowed(si, upisujeGodinu, req.isAdminOverride(), req.getOverrideReason());
        SkolskaGodina aktivna = aktivnaGodina();
        if (upisRepo.existsByIndeksIdAndUpisujeGodinuAndSkolskaGodinaId(si.getId(), upisujeGodinu, aktivna.getId())) {
            throw new IllegalStateException("Student je već upisao ovu godinu studija u aktivnoj školskoj godini.");
        }

        // Validacija predmeta: predmet mora biti na programu i ne sme biti iz "više" godine od upisa
        UpisGodine upis = new UpisGodine();
        upis.setIndeks(si);
        upis.setDatum(LocalDate.now());
        upis.setUpisujeGodinu(upisujeGodinu);
        upis.setNapomena("Upis preko API-ja");
        upis.setSkolskaGodina(aktivna);
        upis = upisRepo.save(upis);

        slusaPredmetService.enrollRealizations(si, upis,
                realizacijaService.ensureForEnrollment(si.getStudijskiProgram().getId(), upisujeGodinu));

        return upis.getId();
    }

    @Transactional
    public int syncCurrentSubjects(Long indeksId) {
        StudentIndeks indeks = requireIndeks(indeksId);
        SkolskaGodina activeSchoolYear = aktivnaGodina();
        UpisGodine currentEnrollment = upisRepo.findUpisi(indeksId).stream()
                .filter(upis -> upis.getSkolskaGodina() != null
                        && activeSchoolYear.getId().equals(upis.getSkolskaGodina().getId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Student nema upis godine u aktivnoj skolskoj godini."));

        slusaPredmetService.enrollRealizations(indeks, currentEnrollment,
                realizacijaService.ensureForEnrollment(
                        indeks.getStudijskiProgram().getId(), currentEnrollment.getUpisujeGodinu()));
        return slusaRepo.getSlusaPredmetForIndeksAktivnaGodina(indeksId).size();
    }

    @Transactional
    public Long obnova(ObnovaCreateRequest req) {
        StudentIndeks si = requireIndeks(req.getIndeksId());
        SkolskaGodina aktivna = aktivnaGodina();

        int obnavljaGodinu = req.getObnavljaGodinu();
        int maxGodina = programTrajanje(si);
        if (obnavljaGodinu < 1 || obnavljaGodinu > maxGodina) {
            throw new IllegalArgumentException("obnavljaGodinu mora biti u opsegu [1.." + maxGodina + "].");
        }

        List<DrziPredmet> dps = req.getDrziPredmetIds().stream()
                .distinct()
                .map(this::requireDrzi)
                .collect(Collectors.toList());

        // Validacija predmeta: predmet mora biti na programu i ne sme biti iz "više" godine od obnove
        validatePredmetiZaGodinu(si, obnavljaGodinu, dps);

        int ukupnoEspb = dps.stream()
                .map(dp -> Optional.ofNullable(dp.getPredmet()).map(p -> Optional.ofNullable(p.getEspb()).orElse(0)).orElse(0))
                .reduce(0, Integer::sum);

        if (ukupnoEspb > 60) {
            throw new IllegalArgumentException("Maksimalno dozvoljeno 60 ESPB pri obnovi (pokušano: " + ukupnoEspb + ").");
        }

        ObnovaGodine o = new ObnovaGodine();
        o.setIndeks(si);
        o.setDatum(LocalDate.now());

        // ISPRAVKA: više ne ide si.getGodina() (to je godina upisa indeksa),
        // nego eksplicitna godina studija iz request-a
        o.setObnavljaGodinu(obnavljaGodinu);

        o.setNapomena("Obnova (API)");
        ObnovaGodine saved = obnovaRepo.save(o);

        for (DrziPredmet dp : dps) {
            if (dp.getRealizacijaPredmeta() == null) {
                throw new IllegalStateException("DrziPredmet nije vezan za realizaciju predmeta.");
            }
            if (!slusaRepo.existsByStudentIndeksIdAndRealizacijaPredmetaId(si.getId(), dp.getRealizacijaPredmeta().getId())) {
                SlusaPredmet sp = new SlusaPredmet();
                sp.setStudentIndeks(si);
                sp.setDrziPredmet(dp);
                if (dp.getRealizacijaPredmeta() == null) {
                    throw new IllegalStateException("DržiPredmet nije vezan za realizaciju predmeta.");
                }
                sp.setRealizacijaPredmeta(dp.getRealizacijaPredmeta());
                sp.setSkolskaGodina(dp.getRealizacijaPredmeta().getSkolskaGodina());
                slusaRepo.save(sp);
            }
        }

        return saved.getId();
    }

    // ----------------- Helpers -----------------

    private int programTrajanje(StudentIndeks si) {
        // “kao da ima 4 godine” – ako program nema trajanje, tretiramo kao 4
        if (si.getStudijskiProgram() == null) return 4;
        return Optional.ofNullable(si.getStudijskiProgram().getTrajanjeGodina()).orElse(4);
    }

    private int odrediGodinuStudija(Long indeksId) {
        List<UpisGodine> upisi = upisRepo.findUpisi(indeksId);
        if (upisi.isEmpty()) return 1;

        int max = upisi.stream()
                .mapToInt(UpisGodine::getUpisujeGodinu)
                .max()
                .orElse(1);

        return max + 1;
    }

    private void validatePredmetiZaGodinu(StudentIndeks si, int godinaStudija, List<DrziPredmet> dps) {
        if (si.getStudijskiProgram() == null || si.getStudijskiProgram().getId() == null) {
            throw new IllegalStateException("Indeks nema vezan studijski program – ne mogu da validiram predmete.");
        }

        Long programId = si.getStudijskiProgram().getId();

        for (DrziPredmet dp : dps) {
            if (dp.getPredmet() == null || dp.getPredmet().getId() == null) {
                throw new IllegalArgumentException("Neispravan DrziPredmet: nema predmet.");
            }

            Long predmetId = dp.getPredmet().getId();
            ProgramPredmet pp = ppRepo.findByProgramIdAndPredmetId(programId, predmetId)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Predmet (id=" + predmetId + ") nije definisan na programu (id=" + programId + ")."
                    ));

            // dozvoljavamo <= (npr. upis 2. godine može da ima zaostatke iz 1.)
            if (pp.getGodinaStudija() != null && pp.getGodinaStudija() > godinaStudija) {
                throw new IllegalArgumentException(
                        "Predmet '" + dp.getPredmet().getNaziv() + "' je sa " + pp.getGodinaStudija() +
                                ". godine, ne može u upisu/obnovi " + godinaStudija + ". godine."
                );
            }
        }
    }

    private StudentIndeks requireIndeks(Long id) {
        return indeksRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ne postoji indeks sa id=" + id));
    }

    private DrziPredmet requireDrzi(Long id) {
        return drziPredRepo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Ne postoji DrziPredmet sa id=" + id));
    }

    private SkolskaGodina aktivnaGodina() {
        SkolskaGodina sg = sgRepo.findFirstByAktivnaTrue();
        if (sg == null) throw new IllegalStateException("Nije definisana aktivna školska godina.");
        return sg;
    }
}
