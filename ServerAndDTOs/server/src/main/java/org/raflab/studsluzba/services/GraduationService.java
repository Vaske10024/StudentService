package org.raflab.studsluzba.services;
import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.academic.GraduationRecord;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.academic.GraduationRecordRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.security.CurrentUser;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @RequiredArgsConstructor @Transactional
public class GraduationService {
    private final GraduationRecordRepository repo;
    private final StudentIndeksRepository indeksRepo;
    private final AcademicProgressService progressService;
    private final StudentLifecycleService lifecycleService;
    private final CurrentUser currentUser;
    public GraduationRecord graduate(Long indeksId, String note) {
        currentUser.requireAdmin();
        if (repo.existsByStudentIndeksId(indeksId)) throw ApiException.conflict("ALREADY_GRADUATED","Diplomiranje je vec evidentirano.");
        StudentIndeks indeks=indeksRepo.findById(indeksId).orElseThrow(()->ApiException.notFound("Indeks ne postoji."));
        int earned=indeks.getOstvarenoEspb()==null?0:indeks.getOstvarenoEspb();
        int required=indeks.getStudijskiProgram()==null||indeks.getStudijskiProgram().getUkupnoEspb()==null?0:indeks.getStudijskiProgram().getUkupnoEspb();
        if(earned<required) throw ApiException.conflict("GRADUATION_REQUIREMENTS_NOT_MET","Student nema sve potrebne ESPB.");
        GraduationRecord record=new GraduationRecord();
        record.setStudentIndeks(indeks); record.setEarnedEcts(earned); record.setAverageGrade(progressService.averageGrade(indeksId));
        record.setNote(note); record.setApprovedByUserId(currentUser.userId());
        repo.save(record);
        lifecycleService.markGraduated(indeksId, note==null?"Diplomiranje":note);
        return record;
    }
}
