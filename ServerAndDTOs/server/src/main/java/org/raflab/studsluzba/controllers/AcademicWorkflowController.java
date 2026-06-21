package org.raflab.studsluzba.controllers;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.academic.GraduationRecord;
import org.raflab.studsluzba.model.academic.ProgramTransfer;
import org.raflab.studsluzba.model.academic.RecognizedSubject;
import org.raflab.studsluzba.model.dtos.GraduationRecordDTO;
import org.raflab.studsluzba.model.dtos.ProgramTransferDTO;
import org.raflab.studsluzba.model.dtos.RecognizedSubjectDTO;
import org.raflab.studsluzba.services.GraduationService;
import org.raflab.studsluzba.services.ProgramTransferService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/academic")
@RequiredArgsConstructor
public class AcademicWorkflowController {
    private final GraduationService graduationService;
    private final ProgramTransferService transferService;

    @PostMapping("/graduation/{indeksId}")
    public GraduationRecordDTO graduate(@PathVariable Long indeksId,
                                        @RequestParam(required = false) String note) {
        return toDto(graduationService.graduate(indeksId, note));
    }

    @PostMapping("/transfers")
    public ProgramTransferDTO request(@RequestParam Long indeksId,
                                      @RequestParam Long toProgramId,
                                      @RequestParam String reason) {
        return toDto(transferService.request(indeksId, toProgramId, reason));
    }

    @PostMapping("/transfers/{id}/approve")
    public ProgramTransferDTO approve(@PathVariable Long id, @RequestParam BigDecimal newTuitionEur) {
        return toDto(transferService.approve(id, newTuitionEur));
    }

    @PostMapping("/recognized-subjects")
    public RecognizedSubjectDTO recognize(@RequestParam Long indeksId,
                                          @RequestParam Long subjectId,
                                          @RequestParam Integer grade,
                                          @RequestParam String source) {
        return toDto(transferService.recognize(indeksId, subjectId, grade, source));
    }

    private GraduationRecordDTO toDto(GraduationRecord record) {
        return new GraduationRecordDTO(
                record.getId(),
                record.getStudentIndeks() == null ? null : record.getStudentIndeks().getId(),
                record.getEarnedEcts(),
                record.getAverageGrade(),
                record.getNote(),
                record.getApprovedByUserId(),
                record.getGraduatedAt()
        );
    }

    private ProgramTransferDTO toDto(ProgramTransfer transfer) {
        return new ProgramTransferDTO(
                transfer.getId(),
                transfer.getStudentIndeks() == null ? null : transfer.getStudentIndeks().getId(),
                transfer.getFromProgram() == null ? null : transfer.getFromProgram().getId(),
                transfer.getToProgram() == null ? null : transfer.getToProgram().getId(),
                transfer.getStatus() == null ? null : transfer.getStatus().name(),
                transfer.getReason(),
                transfer.getDecidedByUserId(),
                transfer.getDecidedAt(),
                transfer.getCreatedAt()
        );
    }

    private RecognizedSubjectDTO toDto(RecognizedSubject recognized) {
        return new RecognizedSubjectDTO(
                recognized.getId(),
                recognized.getStudentIndeks() == null ? null : recognized.getStudentIndeks().getId(),
                recognized.getSubject() == null ? null : recognized.getSubject().getId(),
                recognized.getSubject() == null ? null : recognized.getSubject().getSifra(),
                recognized.getSubject() == null ? null : recognized.getSubject().getNaziv(),
                recognized.getGrade(),
                recognized.getEcts(),
                recognized.getSource(),
                recognized.getApprovedByUserId(),
                recognized.getCreatedAt()
        );
    }
}
