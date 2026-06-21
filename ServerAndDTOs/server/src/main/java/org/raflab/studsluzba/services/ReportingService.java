package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.StudentStatus;
import org.raflab.studsluzba.model.finance.LedgerEntry;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Ispit;
import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.PrijavaIspita;
import org.raflab.studsluzba.model.security.Permission;
import org.raflab.studsluzba.repositories.IspitQueryRepository;
import org.raflab.studsluzba.repositories.StudentIndeksRepository;
import org.raflab.studsluzba.repositories.finance.LedgerEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ReportingService {
    private final StudentIndeksRepository indeksRepo;
    private final LedgerEntryRepository ledgerRepo;
    private final IspitQueryRepository examRepo;
    private final PermissionService permissions;

    @Transactional(readOnly = true)
    public byte[] activeStudents() {
        permissions.require(Permission.REPORT_EXPORT);
        StringBuilder csv = new StringBuilder("indeks_id,ime,prezime,ime_prezime,program,godina,broj,status,email_fakultetski,email_privatni,nacin_finansiranja\n");
        for (StudentIndeks indeks : indeksRepo.findAll()) {
            if (indeks.getStatus() == StudentStatus.AKTIVAN || indeks.isAktivan()) {
                StudentPodaci student = indeks.getStudent();
                csv.append(indeks.getId()).append(',')
                        .append(csv(student == null ? null : student.getIme())).append(',')
                        .append(csv(student == null ? null : student.getPrezime())).append(',')
                        .append(csv(fullName(student))).append(',')
                        .append(csv(indeks.getStudProgramOznaka())).append(',')
                        .append(value(indeks.getGodina())).append(',')
                        .append(value(indeks.getBroj())).append(',')
                        .append(csv(indeks.getStatus() == null ? "AKTIVAN" : indeks.getStatus().name())).append(',')
                        .append(csv(student == null ? null : student.getEmailFakultetski())).append(',')
                        .append(csv(student == null ? null : student.getEmailPrivatni())).append(',')
                        .append(csv(indeks.getNacinFinansiranja()))
                        .append('\n');
            }
        }
        return bytes(csv);
    }

    @Transactional(readOnly = true)
    public byte[] debts() {
        permissions.require(Permission.REPORT_EXPORT);
        StringBuilder csv = new StringBuilder("indeks_id,ime,prezime,ime_prezime,program,godina,broj,dug_eur,preplata_eur,saldo_eur,email_fakultetski\n");
        for (StudentIndeks indeks : indeksRepo.findAll()) {
            BigDecimal balance = ledgerRepo.findByStudentIndeksIdOrderByCreatedAtAsc(indeks.getId()).stream()
                    .map(LedgerEntry::getAmountEur)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            StudentPodaci student = indeks.getStudent();
            csv.append(indeks.getId()).append(',')
                    .append(csv(student == null ? null : student.getIme())).append(',')
                    .append(csv(student == null ? null : student.getPrezime())).append(',')
                    .append(csv(fullName(student))).append(',')
                    .append(csv(indeks.getStudProgramOznaka())).append(',')
                    .append(value(indeks.getGodina())).append(',')
                    .append(value(indeks.getBroj())).append(',')
                    .append(balance.max(BigDecimal.ZERO)).append(',')
                    .append(balance.min(BigDecimal.ZERO).abs()).append(',')
                    .append(balance).append(',')
                    .append(csv(student == null ? null : student.getEmailFakultetski()))
                    .append('\n');
        }
        return bytes(csv);
    }

    @Transactional(readOnly = true)
    public byte[] passRates() {
        permissions.require(Permission.REPORT_EXPORT);
        Map<Long, PassRateRow> rows = new TreeMap<>();
        for (PrijavaIspita prijava : examRepo.findAll()) {
            if (Boolean.TRUE.equals(prijava.getPonisteno()) || !prijava.isDaLiJeIzasao() || prijava.getIspit() == null) {
                continue;
            }
            Ispit ispit = prijava.getIspit();
            PassRateRow row = rows.computeIfAbsent(ispit.getId(), id -> new PassRateRow(ispit));
            row.exited++;
            if (prijava.getOcena() >= 6) row.passed++;
            if (prijava.getOcena() > 0) {
                row.graded++;
                row.gradeSum += prijava.getOcena();
            }
        }
        StringBuilder csv = new StringBuilder("ispit_id,predmet_sifra,predmet_naziv,espb,rok_id,rok_od,rok_do,datum,vreme,nastavnik,izasli,polozili,nisu_polozili,prolaznost,prosecna_ocena\n");
        rows.forEach((id, row) -> csv.append(id).append(',')
                .append(csv(row.subjectCode)).append(',')
                .append(csv(row.subjectName)).append(',')
                .append(value(row.ects)).append(',')
                .append(value(row.periodId)).append(',')
                .append(csv(row.periodStart)).append(',')
                .append(csv(row.periodEnd)).append(',')
                .append(csv(row.examDate)).append(',')
                .append(csv(row.examTime)).append(',')
                .append(csv(row.professorName)).append(',')
                .append(row.exited).append(',')
                .append(row.passed).append(',')
                .append(row.exited - row.passed).append(',')
                .append(row.exited == 0 ? "0.00" : String.format(java.util.Locale.US, "%.2f", 100.0 * row.passed / row.exited)).append(',')
                .append(row.graded == 0 ? "" : String.format(java.util.Locale.US, "%.2f", row.gradeSum / (double) row.graded))
                .append('\n'));
        return bytes(csv);
    }

    private byte[] bytes(StringBuilder s) {
        return s.toString().getBytes(StandardCharsets.UTF_8);
    }

    private String csv(String value) {
        return value == null ? "" : "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String fullName(StudentPodaci student) {
        if (student == null) return null;
        return ((student.getIme() == null ? "" : student.getIme()) + " " + (student.getPrezime() == null ? "" : student.getPrezime())).trim();
    }

    private static class PassRateRow {
        private final String subjectCode;
        private final String subjectName;
        private final Integer ects;
        private final Long periodId;
        private final String periodStart;
        private final String periodEnd;
        private final String examDate;
        private final String examTime;
        private final String professorName;
        private int exited;
        private int passed;
        private int graded;
        private int gradeSum;

        private PassRateRow(Ispit exam) {
            Predmet subject = exam.getPredmet();
            if (subject == null && exam.getDrziPredmet() != null) subject = exam.getDrziPredmet().getPredmet();
            DrziPredmet assignment = exam.getDrziPredmet();
            Nastavnik professor = exam.getNastavnik();
            if (professor == null && assignment != null) professor = assignment.getNastavnik();
            IspitniRok period = exam.getIspitniRok();
            this.subjectCode = subject == null ? null : subject.getSifra();
            this.subjectName = subject == null ? null : subject.getNaziv();
            this.ects = subject == null ? null : subject.getEspb();
            this.periodId = period == null ? null : period.getId();
            this.periodStart = period == null || period.getDatumPocetka() == null ? null : period.getDatumPocetka().toString();
            this.periodEnd = period == null || period.getDatumZavrsetka() == null ? null : period.getDatumZavrsetka().toString();
            this.examDate = exam.getDatumOdrzavanja() == null ? null : exam.getDatumOdrzavanja().toString();
            this.examTime = exam.getVremePocetka() == null ? null : exam.getVremePocetka().toString();
            this.professorName = professor == null ? null : ((professor.getIme() == null ? "" : professor.getIme()) + " " + (professor.getPrezime() == null ? "" : professor.getPrezime())).trim();
        }
    }
}
