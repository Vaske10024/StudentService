package org.raflab.studsluzba.utils;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.dtos.IspitRezultatDTO;
import org.raflab.studsluzba.model.dtos.StudentLiteDTO;
import org.raflab.studsluzba.services.PrijavaScoreService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class IspitMappers {

    private final PrijavaScoreService scoreService;

    public StudentLiteDTO toStudentLiteDTO(org.raflab.studsluzba.model.StudentIndeks si) {
        if (si == null) return null;
        StudentLiteDTO dto = new StudentLiteDTO();
        dto.setIndeksId(si.getId());
        dto.setStudProgramOznaka(si.getStudProgramOznaka());
        dto.setGodina(si.getGodina());
        dto.setBroj(si.getBroj());
        if (si.getStudent() != null) {
            dto.setStudentId(si.getStudent().getId());
            dto.setIme(si.getStudent().getIme());
            dto.setPrezime(si.getStudent().getPrezime());
        }
        return dto;
    }

    public IspitRezultatDTO toIspitRezultatDTO(PrijavaIspita pi) {
        if (pi == null) return null;
        IspitRezultatDTO dto = new IspitRezultatDTO();
        dto.setId(pi.getId());
        dto.setIspitId(pi.getIspit() == null ? null : pi.getIspit().getId());
        dto.setStudent(toStudentLiteDTO(pi.getStudent()));
        int ukupno = scoreService.ukupniPoeniZaPrijavu(pi);
        int ispitni = pi.getBrojOsvojenihPoena() == null ? 0 : pi.getBrojOsvojenihPoena();
        dto.setIspitniPoeni(ispitni);
        dto.setPredispitniPoeni(Math.max(0, ukupno - ispitni));
        dto.setUkupniPoeni(ukupno);
        dto.setOcena(pi.getOcena());
        dto.setIzasao(pi.isDaLiJeIzasao());
        dto.setPonisteno(Boolean.TRUE.equals(pi.getPonisteno()));
        dto.setNapomena(pi.getNapomena());
        return dto;
    }
}
