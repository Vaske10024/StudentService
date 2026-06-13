package org.raflab.studsluzba.controllers;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.academic.*;
import org.raflab.studsluzba.services.*;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
@RestController @RequestMapping("/api/academic") @RequiredArgsConstructor
public class AcademicWorkflowController {
    private final GraduationService graduationService;
    private final ProgramTransferService transferService;
    @PostMapping("/graduation/{indeksId}") public GraduationRecord graduate(@PathVariable Long indeksId,@RequestParam(required=false)String note){return graduationService.graduate(indeksId,note);}
    @PostMapping("/transfers") public ProgramTransfer request(@RequestParam Long indeksId,@RequestParam Long toProgramId,@RequestParam String reason){return transferService.request(indeksId,toProgramId,reason);}
    @PostMapping("/transfers/{id}/approve") public ProgramTransfer approve(@PathVariable Long id,@RequestParam BigDecimal newTuitionEur){return transferService.approve(id,newTuitionEur);}
    @PostMapping("/recognized-subjects") public RecognizedSubject recognize(@RequestParam Long indeksId,@RequestParam Long subjectId,@RequestParam Integer grade,@RequestParam String source){return transferService.recognize(indeksId,subjectId,grade,source);}
}
