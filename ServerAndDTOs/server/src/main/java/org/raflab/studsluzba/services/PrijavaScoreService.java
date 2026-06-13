package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.repositories.OstvarenaPredObavRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PrijavaScoreService {

    private final OstvarenaPredObavRepository opoRepo;

    public Integer ukupniPoeniZaPrijavu(PrijavaIspita pi) {

        if (pi == null || pi.getStudent() == null) {
            return 0;
        }

        Long studentId = pi.getStudent().getId();


        Long predmetId = null;
        if (pi.getIspit() != null && pi.getIspit().getDrziPredmet().getPredmet() != null) {
            predmetId = pi.getIspit().getDrziPredmet().getPredmet().getId();
        } else if (pi.getPredmet() != null) {
            predmetId = pi.getPredmet().getId();
        }

        if (predmetId == null) {
            // nema predmeta -> samo ispitni poeni (edge case)
            return safePoeniIspit(pi);
        }

        // Školska godina uzeta preko ispitnog roka
        Long sgId = null;
        if (pi.getIspit() != null &&
                pi.getIspit().getIspitniRok() != null &&
                pi.getIspit().getIspitniRok().getSkolskaGodina() != null) {

            sgId = pi.getIspit().getIspitniRok().getSkolskaGodina().getId();
        }

        int predispit = 0;
        if (sgId != null) {
            Integer s = opoRepo.ostvareniPredispitniPoeni(studentId, predmetId, sgId);
            predispit = s != null ? s : 0;
        }

        int ispiti = safePoeniIspit(pi);

        return predispit + ispiti;
    }

    private int safePoeniIspit(PrijavaIspita pi) {
        return pi.getBrojOsvojenihPoena() != null ? pi.getBrojOsvojenihPoena() : 0;
    }
}
