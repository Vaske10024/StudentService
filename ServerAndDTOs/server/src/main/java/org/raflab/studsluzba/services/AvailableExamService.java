package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.dtos.AvailableExamDTO;
import org.raflab.studsluzba.model.dtos.ExamEligibilityDTO;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AvailableExamService {
    private final IspitRepository ispitRepo;
    private final IspitQueryRepository prijavaRepo;
    private final IspitCommandService commandService;

    public List<AvailableExamDTO> forStudent(Long indeksId) {
        return ispitRepo.findCandidatesForStudent(indeksId).stream().map(exam -> toDto(exam, indeksId)).collect(Collectors.toList());
    }

    private AvailableExamDTO toDto(Ispit exam, Long indeksId) {
        AvailableExamDTO dto = new AvailableExamDTO();
        dto.setExamId(exam.getId()); dto.setExamDate(exam.getDatumOdrzavanja()); dto.setExamTime(exam.getVremePocetka()); dto.setLocked(exam.isZakljucen());
        if (exam.getDrziPredmet() != null) {
            if (exam.getDrziPredmet().getPredmet() != null) {
                dto.setSubjectId(exam.getDrziPredmet().getPredmet().getId());
                dto.setSubjectCode(exam.getDrziPredmet().getPredmet().getSifra());
                dto.setSubjectName(exam.getDrziPredmet().getPredmet().getNaziv());
            }
            if (exam.getDrziPredmet().getNastavnik() != null) {
                dto.setProfessorName(exam.getDrziPredmet().getNastavnik().getIme() + " " + exam.getDrziPredmet().getNastavnik().getPrezime());
            }
        }
        if (exam.getIspitniRok() != null) {
            dto.setPeriodId(exam.getIspitniRok().getId()); dto.setRegistrationStart(exam.getIspitniRok().getRegistrationStart());
            dto.setRegistrationEnd(exam.getIspitniRok().getRegistrationEnd()); dto.setCancellationEnd(exam.getIspitniRok().getCancellationEnd());
            dto.setPeriodActive(exam.getIspitniRok().isActive());
        }
        PrijavaIspita registration = prijavaRepo.findAktivnaPrijava(exam.getId(), indeksId).orElse(null);
        if (registration != null) {
            dto.setActiveRegistrationId(registration.getId()); dto.setEligibilityCode("ALREADY_REGISTERED"); dto.setEligibilityMessage("Ispit je prijavljen.");
            dto.setCancellationAllowed(dto.getCancellationEnd() != null && !LocalDateTime.now().isAfter(dto.getCancellationEnd()));
        } else {
            ExamEligibilityDTO eligibility = commandService.eligibility(exam.getId(), indeksId);
            dto.setEligible(eligibility.isEligible()); dto.setEligibilityCode(eligibility.getCode()); dto.setEligibilityMessage(eligibility.getMessage());
        }
        return dto;
    }
}
