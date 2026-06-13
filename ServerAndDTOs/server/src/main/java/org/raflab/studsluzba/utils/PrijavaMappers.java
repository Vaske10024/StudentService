package org.raflab.studsluzba.utils;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.dtos.PrijavaResponseDTO;
import org.raflab.studsluzba.services.PrijavaScoreService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PrijavaMappers {

    private final PrijavaScoreService scoreService;
    private final IspitMappers ispitMappers;

    public PrijavaResponseDTO toResponse(PrijavaIspita pi){
        if (pi == null) return null;

        PrijavaResponseDTO dto = new PrijavaResponseDTO();
        dto.setId(pi.getId());


        dto.setIspitId(pi.getIspit() != null ? pi.getIspit().getId() : null);
        Predmet predmet = pi.getPredmet();
        if (predmet == null && pi.getIspit() != null) {
            predmet = pi.getIspit().getPredmet();
            if (predmet == null && pi.getIspit().getDrziPredmet() != null) {
                predmet = pi.getIspit().getDrziPredmet().getPredmet();
            }
        }
        if (predmet != null) {
            dto.setPredmetId(predmet.getId());
            dto.setPredmetSifra(predmet.getSifra());
            dto.setPredmetNaziv(predmet.getNaziv());
        }
        if (pi.getIspit() != null) {
            dto.setDatumIspita(pi.getIspit().getDatumOdrzavanja());
            dto.setVremePocetka(pi.getIspit().getVremePocetka());
            if (pi.getIspit().getNastavnik() != null) {
                dto.setNastavnikImePrezime(pi.getIspit().getNastavnik().getIme() + " "
                        + pi.getIspit().getNastavnik().getPrezime());
            } else if (pi.getIspit().getDrziPredmet() != null
                    && pi.getIspit().getDrziPredmet().getNastavnik() != null) {
                dto.setNastavnikImePrezime(pi.getIspit().getDrziPredmet().getNastavnik().getIme() + " "
                        + pi.getIspit().getDrziPredmet().getNastavnik().getPrezime());
            }
        }
        dto.setDatumPrijave(pi.getDatumPrijave());
        dto.setStudent(ispitMappers.toStudentLiteDTO(pi.getStudent()));

        Integer ukupno = scoreService.ukupniPoeniZaPrijavu(pi);
        Integer ispitni = pi.getBrojOsvojenihPoena();
        dto.setIspitniPoeni(ispitni == null ? 0 : ispitni);

        int pre = Math.max(0, (ukupno == null ? 0 : ukupno) - dto.getIspitniPoeni());
        dto.setPredispitniPoeni(pre);
        dto.setUkupnoPoena((ukupno == null) ? dto.getIspitniPoeni() + pre : ukupno);

        dto.setOcena(pi.getOcena());
        dto.setIzasao(pi.isDaLiJeIzasao());
        dto.setPonisteno(Boolean.TRUE.equals(pi.getPonisteno()));
        dto.setNapomena(pi.getNapomena());
        dto.setStatus(pi.getStatus() == null ? null : pi.getStatus().name());
        dto.setCancelledAt(pi.getCancelledAt());
        dto.setCancellationReason(pi.getCancellationReason());
        return dto;
    }
}
