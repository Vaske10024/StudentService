package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.academic.*;
import org.raflab.studsluzba.model.finance.FinancingType;
import org.raflab.studsluzba.model.ispiti.*;
import org.raflab.studsluzba.repositories.*;
import org.raflab.studsluzba.repositories.academic.*;
import org.raflab.studsluzba.security.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
@Service @RequiredArgsConstructor @Transactional
public class ProgramTransferService {
    private final ProgramTransferRepository transferRepo;
    private final RecognizedSubjectRepository recognizedRepo;
    private final StudentIndeksRepository indeksRepo;
    private final StudijskiProgramRepository programRepo;
    private final PredmetRepository predmetRepo;
    private final TuitionService tuitionService;
    private final IspitCommandService examService;
    private final CurrentUser currentUser;
    public ProgramTransfer request(Long indeksId,Long toProgramId,String reason){
        currentUser.requireAdminOrStudentOwnsIndeks(indeksId);
        StudentIndeks i=indeksRepo.findById(indeksId).orElseThrow(()->ApiException.notFound("Indeks ne postoji."));
        StudijskiProgram to=programRepo.findById(toProgramId).orElseThrow(()->ApiException.notFound("Program ne postoji."));
        ProgramTransfer t=new ProgramTransfer();t.setStudentIndeks(i);t.setFromProgram(i.getStudijskiProgram());t.setToProgram(to);t.setReason(reason);return transferRepo.save(t);
    }
    public ProgramTransfer approve(Long id,BigDecimal newTuitionEur){
        currentUser.requireAdmin(); ProgramTransfer t=transferRepo.findById(id).orElseThrow(()->ApiException.notFound("Transfer ne postoji."));
        if(t.getStatus()!=ProgramTransfer.Status.REQUESTED)throw ApiException.conflict("TRANSFER_ALREADY_DECIDED","Transfer je vec obradjen.");
        StudentIndeks i=t.getStudentIndeks();i.setStudijskiProgram(t.getToProgram());i.setStudProgramOznaka(t.getToProgram().getOznaka());indeksRepo.save(i);
        tuitionService.createInitialPlan(i,FinancingType.SELF_FINANCED,newTuitionEur);
        t.setStatus(ProgramTransfer.Status.APPROVED);t.setDecidedAt(LocalDateTime.now());t.setDecidedByUserId(currentUser.userId());return transferRepo.save(t);
    }
    public RecognizedSubject recognize(Long indeksId,Long subjectId,Integer grade,String source){
        currentUser.requireAdmin(); StudentIndeks i=indeksRepo.findById(indeksId).orElseThrow(()->ApiException.notFound("Indeks ne postoji."));
        Predmet p=predmetRepo.findById(subjectId).orElseThrow(()->ApiException.notFound("Predmet ne postoji."));
        examService.priznajPredmet(indeksId,subjectId,grade,source);
        RecognizedSubject r=new RecognizedSubject();r.setStudentIndeks(i);r.setSubject(p);r.setGrade(grade);r.setEcts(p.getEspb());r.setSource(source);r.setApprovedByUserId(currentUser.userId());return recognizedRepo.save(r);
    }
}
