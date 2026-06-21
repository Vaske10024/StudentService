package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.documents.GeneratedCertificate;
import org.raflab.studsluzba.model.documents.RequestType;
import org.raflab.studsluzba.model.documents.StudentRequest;
import org.raflab.studsluzba.repositories.documents.GeneratedCertificateRepository;
import org.raflab.studsluzba.repositories.documents.StudentDocumentRepository;
import org.raflab.studsluzba.model.documents.StudentDocument;
import org.raflab.studsluzba.model.documents.DocumentType;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CertificateGenerationService {
    private final FileStorageService storage;
    private final GeneratedCertificateRepository repo;
    private final StudentDocumentRepository documentRepo;
    private final StudentIspitiViewService ispitiViewService;

    public GeneratedCertificate generate(StudentRequest request) {
        String code = UUID.randomUUID().toString();
        byte[] bytes = buildPdf(request, code);
        GeneratedCertificate certificate = new GeneratedCertificate();
        certificate.setStudentRequest(request);
        certificate.setVerificationCode(code);
        certificate.setStorageKey(storage.store(bytes, "application/pdf"));
        GeneratedCertificate saved = repo.save(certificate);
        StudentDocument document = new StudentDocument();
        document.setStudentRequest(request);
        document.setStudentIndeks(request.getStudentIndeks());
        document.setType(DocumentType.CERTIFICATE);
        document.setOriginalName(fileName(request));
        document.setContentType("application/pdf");
        document.setSizeBytes(bytes.length);
        document.setStorageKey(saved.getStorageKey());
        documentRepo.save(document);
        return saved;
    }

    private byte[] buildPdf(StudentRequest request, String verificationCode) {
        String title = request.getType() == RequestType.UVERENJE_O_POLOZENIM_ISPITIMA
                ? "Uverenje o polozenim ispitima"
                : "Potvrda o studiranju";
        List<String> lines = new ArrayList<>();
        StudentIndeks indeks = request.getStudentIndeks();
        StudentPodaci student = indeks == null ? null : indeks.getStudent();
        lines.add("Broj zahteva: " + value(request.getId()));
        lines.add("Datum izdavanja: " + LocalDate.now());
        lines.add("Verifikacioni kod: " + verificationCode);
        lines.add("");
        lines.add("Student: " + fullName(student));
        lines.add("Indeks: " + indexLabel(indeks));
        lines.add("Studijski program: " + value(indeks == null ? null : indeks.getStudProgramOznaka()));
        lines.add("Status: " + value(indeks == null || indeks.getStatus() == null ? null : indeks.getStatus().name()));
        lines.add("Nacin finansiranja: " + value(indeks == null ? null : indeks.getNacinFinansiranja()));
        lines.add("");

        if (request.getType() == RequestType.UVERENJE_O_POLOZENIM_ISPITIMA) {
            addPassedExamList(lines, indeks);
        } else {
            lines.add("Potvrdjuje se da je gore navedeni student evidentiran u informacionom");
            lines.add("sistemu studentske sluzbe kao student sa navedenim indeksom i statusom.");
            lines.add("");
            lines.add("Svrha izdavanja: " + value(request.getReason()));
        }

        lines.add("");
        lines.add("Studentska sluzba");
        return createPdf(title, lines);
    }

    private void addPassedExamList(List<String> lines, StudentIndeks indeks) {
        if (indeks == null) {
            lines.add("Nema dostupnog indeksa za izradu spiska polozenih ispita.");
            return;
        }
        var page = ispitiViewService.polozenePaged(indeks.getStudProgramOznaka(), indeks.getGodina(), indeks.getBroj(), 0, 1000);
        var passed = page.getContent();
        int totalEcts = passed.stream().mapToInt(item -> item.getEspb() == null ? 0 : item.getEspb()).sum();
        long gradeCount = passed.stream().filter(item -> item.getOcena() != null).count();
        BigDecimal average = passed.stream()
                .filter(item -> item.getOcena() != null)
                .map(item -> BigDecimal.valueOf(item.getOcena()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        lines.add("Ukupno polozenih predmeta: " + passed.size());
        lines.add("Ukupno ESPB: " + totalEcts);
        lines.add("Prosecna ocena: " + (gradeCount == 0 ? "" : average.divide(BigDecimal.valueOf(gradeCount), 2, RoundingMode.HALF_UP)));
        lines.add("");
        lines.add(String.format(Locale.US, "%-10s %-38s %5s %5s %12s", "Sifra", "Predmet", "ESPB", "Oc.", "Datum"));
        lines.add(repeat("-", 76));
        passed.forEach(item -> lines.add(String.format(Locale.US, "%-10s %-38s %5s %5s %12s",
                clip(item.getSifra(), 10),
                clip(item.getNaziv(), 38),
                value(item.getEspb()),
                value(item.getOcena()),
                value(item.getDatum()))));
        if (passed.isEmpty()) {
            lines.add("Nema evidentiranih polozenih ispita.");
        }
    }

    private byte[] createPdf(String title, List<String> rawLines) {
        List<List<String>> pages = paginate(rawLines, 46);
        List<String> objects = new ArrayList<>();
        objects.add("<< /Type /Catalog /Pages 2 0 R >>");
        objects.add("");
        objects.add("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica >>");
        List<Integer> pageIds = new ArrayList<>();

        for (int page = 0; page < pages.size(); page++) {
            String stream = pageStream(title, pages.get(page), page + 1, pages.size());
            int contentId = objects.size() + 1;
            objects.add("<< /Length " + stream.getBytes(StandardCharsets.US_ASCII).length + " >>\nstream\n" + stream + "endstream");
            int pageId = objects.size() + 1;
            objects.add("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 3 0 R >> >> /Contents " + contentId + " 0 R >>");
            pageIds.add(pageId);
        }

        StringBuilder kids = new StringBuilder();
        for (Integer pageId : pageIds) kids.append(pageId).append(" 0 R ");
        objects.set(1, "<< /Type /Pages /Kids [" + kids + "] /Count " + pageIds.size() + " >>");

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        write(out, "%PDF-1.4\n");
        List<Integer> offsets = new ArrayList<>();
        offsets.add(0);
        for (int i = 0; i < objects.size(); i++) {
            offsets.add(out.size());
            write(out, (i + 1) + " 0 obj\n" + objects.get(i) + "\nendobj\n");
        }
        int xref = out.size();
        write(out, "xref\n0 " + (objects.size() + 1) + "\n");
        write(out, "0000000000 65535 f \n");
        for (int i = 1; i < offsets.size(); i++) {
            write(out, String.format(Locale.US, "%010d 00000 n \n", offsets.get(i)));
        }
        write(out, "trailer\n<< /Size " + (objects.size() + 1) + " /Root 1 0 R >>\nstartxref\n" + xref + "\n%%EOF\n");
        return out.toByteArray();
    }

    private List<List<String>> paginate(List<String> lines, int pageSize) {
        List<List<String>> pages = new ArrayList<>();
        List<String> current = new ArrayList<>();
        for (String line : lines) {
            for (String wrapped : wrap(ascii(line), 92)) {
                if (current.size() >= pageSize) {
                    pages.add(current);
                    current = new ArrayList<>();
                }
                current.add(wrapped);
            }
        }
        if (current.isEmpty()) current.add("");
        pages.add(current);
        return pages;
    }

    private String pageStream(String title, List<String> lines, int page, int totalPages) {
        StringBuilder stream = new StringBuilder();
        stream.append("BT /F1 16 Tf 72 790 Td (").append(escape(ascii(title))).append(") Tj ET\n");
        int y = 760;
        for (String line : lines) {
            stream.append("BT /F1 10 Tf 72 ").append(y).append(" Td (").append(escape(line)).append(") Tj ET\n");
            y -= 14;
        }
        stream.append("BT /F1 9 Tf 72 42 Td (Strana ").append(page).append("/").append(totalPages).append(") Tj ET\n");
        return stream.toString();
    }

    private List<String> wrap(String text, int max) {
        List<String> lines = new ArrayList<>();
        String remaining = text == null ? "" : text;
        while (remaining.length() > max) {
            int cut = remaining.lastIndexOf(' ', max);
            if (cut < 30) cut = max;
            lines.add(remaining.substring(0, cut).trim());
            remaining = remaining.substring(cut).trim();
        }
        lines.add(remaining);
        return lines;
    }

    private void write(ByteArrayOutputStream out, String text) {
        byte[] bytes = text.getBytes(StandardCharsets.US_ASCII);
        out.write(bytes, 0, bytes.length);
    }

    private String fileName(StudentRequest request) {
        String prefix = request.getType() == RequestType.UVERENJE_O_POLOZENIM_ISPITIMA ? "uverenje-o-polozenim-ispitima" : "potvrda-o-studiranju";
        return prefix + "-" + request.getId() + ".pdf";
    }

    private String fullName(StudentPodaci student) {
        if (student == null) return "";
        return ((student.getIme() == null ? "" : student.getIme()) + " " + (student.getPrezime() == null ? "" : student.getPrezime())).trim();
    }

    private String indexLabel(StudentIndeks indeks) {
        if (indeks == null) return "";
        return value(indeks.getStudProgramOznaka()) + " " + value(indeks.getBroj()) + "/" + value(indeks.getGodina());
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String clip(String value, int max) {
        String ascii = ascii(value);
        return ascii.length() <= max ? ascii : ascii.substring(0, Math.max(0, max - 3)) + "...";
    }

    private String ascii(String value) {
        if (value == null) return "";
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD).replaceAll("\\p{M}", "");
        return normalized.replaceAll("[^\\x20-\\x7E]", "?");
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("(", "\\(").replace(")", "\\)");
    }

    private String repeat(String value, int count) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < count; i++) builder.append(value);
        return builder.toString();
    }
}
