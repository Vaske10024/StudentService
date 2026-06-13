package org.raflab.studsluzba.services;
//asdasd
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.dtos.PolozenPredmetDTO;
import org.raflab.studsluzba.model.dtos.PredmetDTO;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.ProgramPredmetRepository;
import org.raflab.studsluzba.repositories.SlusaPredmetRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class
StudentIspitiViewService {

    private final IspitQueryRepository iqRepo;
    private final ProgramPredmetRepository programPredmetRepo;
    private final StudentIndeksRepository indeksRepo;
    private final SlusaPredmetRepository slusaRepo;


    @Transactional
    public Page<PolozenPredmetDTO> polozenePaged(
            String sp, int godina, int broj, int page, int size) {

        // Položeni ispitom
        List<PolozenPredmetDTO> viaIspit =
                iqRepo.polozeniByIndex(sp, godina, broj)
                        .stream()
                        .map(pi -> {
                            Predmet p =
                                    pi.getPredmet() != null
                                            ? pi.getPredmet()
                                            : (pi.getIspit() != null ? pi.getIspit().getDrziPredmet().getPredmet() : null);

                            if (p == null) return null;

                            return new PolozenPredmetDTO(
                                    p.getId(),
                                    p.getSifra(),
                                    p.getNaziv(),
                                    p.getEspb(),
                                    pi.getOcena(),
                                    "ISPIT",
                                    pi.getDatumPrijave()

                            );
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        // Priznati predmeti
        List<PolozenPredmetDTO> viaPriznat =
                iqRepo.priznatiByIndex(sp, godina, broj)
                        .stream()
                        .map(pi -> {
                            Predmet p = pi.getPredmet();
                            if (p == null) return null;

                            return new PolozenPredmetDTO(
                                    p.getId(),
                                    p.getSifra(),
                                    p.getNaziv(),
                                    p.getEspb(),
                                    pi.getOcena(),
                                    "PRIZNAT",
                                    pi.getDatumPrijave()
                            );
                        })
                        .filter(Objects::nonNull)
                        .collect(Collectors.toList());

        // Spajanje
        List<PolozenPredmetDTO> all = new ArrayList<>();
        all.addAll(viaIspit);
        all.addAll(viaPriznat);

        // Sort
        all.sort(Comparator
                .comparing(PolozenPredmetDTO::getSifra, Comparator.nullsLast(String::compareTo))
                .thenComparing(PolozenPredmetDTO::getNacin)
                .thenComparing(PolozenPredmetDTO::getDatum, Comparator.nullsLast(Comparator.naturalOrder()))
        );

        // Paginacija
        int from = Math.min(page * size, all.size());
        int to = Math.min(from + size, all.size());

        return new PageImpl<>(all.subList(from, to), PageRequest.of(page, size), all.size());
    }


    @Transactional
    public Page<PredmetDTO> nepolozeniPaged(String sp, int godina, int broj, int page, int size) {

        var si = indeksRepo.findStudentIndeks(sp, godina, broj);
        if (si == null) {
            throw new NoSuchElementException("Ne postoji indeks: " + sp + " " + godina + " " + broj);
        }
        Long indeksId = si.getId();

        // ----------------------------
        // POLOŽENI ISPITOM
        // ----------------------------
        List<Long> polozeniIspitom = iqRepo.polozeniByIndex(sp, godina, broj)
                .stream()
                .map(pi -> {
                    Predmet p = (pi.getIspit() != null) ? pi.getIspit().getDrziPredmet().getPredmet() : pi.getPredmet();
                    return (p != null) ? p.getId() : null;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        // ----------------------------
        // PRIZNATI PREDMETI
        // ----------------------------
        List<Long> priznatiPredmeti = iqRepo.priznatiByIndex(sp, godina, broj)
                .stream()
                .map(pi -> (pi.getPredmet() != null) ? pi.getPredmet().getId() : null)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet())
                .stream().collect(Collectors.toList());

        // ----------------------------
        // UKUPAN SET POLOŽENIH
        // ----------------------------
        Set<Long> polozeni = new HashSet<>();
        polozeni.addAll(polozeniIspitom);
        polozeni.addAll(priznatiPredmeti);

        // ----------------------------
        // PREDMETI KOJE STUDENT SLUŠA OVE GODINE
        // ----------------------------
        List<SlusaPredmet> slusaList = slusaRepo.getSlusaPredmetForIndeksAktivnaGodina(indeksId);

        // ----------------------------
        // MAPIRANJE U NEPOLOŽENE
        // ----------------------------
        List<PredmetDTO> nepolozeni = slusaList.stream()
                .map(spEnt -> {
                    if (spEnt == null || spEnt.getRealizacijaPredmeta() == null
                            || spEnt.getRealizacijaPredmeta().getProgramPredmet() == null)
                        return null;

                    Predmet p = spEnt.getRealizacijaPredmeta().getProgramPredmet().getPredmet();
                    return p;
                })
                .filter(Objects::nonNull)
                .filter(p -> !polozeni.contains(p.getId()))
                .distinct()
                .map(p -> new PredmetDTO(
                        p.getId(),
                        p.getSifra(),
                        p.getNaziv(),
                        p.getOpis(),
                        p.getEspb(),
                        p.getStudProgram() != null ? p.getStudProgram().getOznaka() : null
                ))
                .collect(Collectors.toList());


        int from = Math.min(page * size, nepolozeni.size());
        int to = Math.min(from + size, nepolozeni.size());

        return new PageImpl<>(nepolozeni.subList(from, to),
                PageRequest.of(page, size),
                nepolozeni.size());
    }


}
