package org.raflab.studsluzba.utils;

import org.raflab.studsluzba.model.StudentIndeks;
import org.raflab.studsluzba.model.StudentPodaci;
import org.raflab.studsluzba.model.Nastavnik;
import org.raflab.studsluzba.model.ispiti.DrziPredmet;
import org.raflab.studsluzba.model.ispiti.Predmet;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.ispiti.SlusaPredmet;
import org.raflab.studsluzba.model.ispiti.StudijskiProgram;
import org.raflab.studsluzba.model.VrstaStudija;
import org.raflab.studsluzba.model.dtos.*;

import org.raflab.studsluzba.repositories.StudentPodaciRepository;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class EntityMappers {


    private final StudentPodaciRepository studentPodaciRepository; // DODAJ

    public EntityMappers(StudentPodaciRepository studentPodaciRepository) { // DODAJ
        this.studentPodaciRepository = studentPodaciRepository;
    }


    public StudentDTO fromStudentPodaciToDTO(StudentPodaci sp) {
        if (sp == null) return null;

        StudentDTO s = new StudentDTO();
        s.setIdStudentPodaci(sp.getId());
        s.setIme(sp.getIme());
        s.setPrezime(sp.getPrezime());

        // --- DODAJ OVO: indeks info (da client može da otvori profil)
        List<StudentIndeks> aktivni = studentPodaciRepository.getAktivanIndeks(sp.getId());
        StudentIndeks si = (aktivni != null && !aktivni.isEmpty())
                ? aktivni.get(0)
                : null;

        if (si == null) {
            List<StudentIndeks> neaktivni = studentPodaciRepository.getNeaktivniIndeksi(sp.getId());
            if (neaktivni != null && !neaktivni.isEmpty()) {
                si = neaktivni.get(0); // ili izaberi “najnoviji” ako hoćeš
            }
        }

        if (si != null) {
            s.setIdIndeks(si.getId());
            s.setStudProgramOznaka(si.getStudProgramOznaka());
            s.setGodinaUpisa(si.getGodina());
            s.setBroj(si.getBroj());
            s.setAktivanIndeks(si.isAktivan());
        }

        return s;
    }


    public StudentDTO fromStudentIndeksToDTO(StudentIndeks si) {
        if (si == null) return null;
        StudentDTO s = fromStudentPodaciToDTO(si.getStudent());
        if (s == null) s = new StudentDTO();
        s.setIdIndeks(si.getId());
        s.setGodinaUpisa(si.getGodina());
        s.setBroj(si.getBroj());
        s.setStudProgramOznaka(si.getStudProgramOznaka());
        s.setAktivanIndeks(si.isAktivan());
        return s;
    }

    public StudentIndeksResponse fromStudentIndexToResponse(StudentIndeks si) {
        if (si == null) return null;

        StudentIndeksResponse response = new StudentIndeksResponse();
        response.setId(si.getId());
        response.setBroj(si.getBroj() == null ? 0 : si.getBroj());
        response.setGodina(si.getGodina() == null ? 0 : si.getGodina());
        response.setStudProgramOznaka(si.getStudProgramOznaka());
        response.setNacinFinansiranja(si.getNacinFinansiranja());
        response.setAktivan(si.isAktivan());
        response.setVaziOd(si.getVaziOd());
        response.setOstvarenoEspb(si.getOstvarenoEspb());

        // DTO umesto entiteta
        response.setStudijskiProgram(fromStudijskiProgramToDTO(si.getStudijskiProgram()));

        response.setStudent(fromStudentPodaciToResponse(si.getStudent()));
        return response;
    }

    public StudentIndeksLiteDTO fromStudentIndexToLiteDTO(StudentIndeks si) {
        if (si == null) return null;
        StudentIndeksLiteDTO dto = new StudentIndeksLiteDTO();
        dto.setId(si.getId());
        dto.setBroj(si.getBroj());
        dto.setGodina(si.getGodina());
        dto.setStudProgramOznaka(si.getStudProgramOznaka());
        if (si.getStudent() != null) {
            dto.setStudentId(si.getStudent().getId());
            dto.setIme(si.getStudent().getIme());
            dto.setPrezime(si.getStudent().getPrezime());
        }
        return dto;
    }

    public StudentPodaciResponse fromStudentPodaciToResponse(StudentPodaci sp) {
        if (sp == null) return null;
        StudentPodaciResponse response = new StudentPodaciResponse();
        response.setId(sp.getId());
        response.setIme(sp.getIme());
        response.setPrezime(sp.getPrezime());
        response.setSrednjeIme(sp.getSrednjeIme());
        response.setJmbg(sp.getJmbg());
        response.setDatumRodjenja(sp.getDatumRodjenja());
        response.setMestoRodjenja(sp.getMestoRodjenja());
        response.setMestoPrebivalista(sp.getMestoPrebivalista());
        response.setDrzavaRodjenja(sp.getDrzavaRodjenja());
        response.setDrzavljanstvo(sp.getDrzavljanstvo());
        response.setNacionalnost(sp.getNacionalnost());
        response.setPol(sp.getPol());
        response.setAdresa(sp.getAdresa());
        response.setBrojTelefonaMobilni(sp.getBrojTelefonaMobilni());
        response.setBrojTelefonaFiksni(sp.getBrojTelefonaFiksni());
        response.setEmailPrivatni(sp.getEmailPrivatni());
        response.setEmailFakultetski(sp.getEmailFakultetski());
        response.setBrojLicneKarte(sp.getBrojLicneKarte());
        response.setLicnuKartuIzdao(sp.getLicnuKartuIzdao());
        response.setMestoStanovanja(sp.getMestoStanovanja());
        response.setAdresaStanovanja(sp.getAdresaStanovanja());
        return response;
    }

    public VrstaStudijaDTO fromVrstaStudijaToDTO(VrstaStudija vs) {
        if (vs == null) return null;
        return new VrstaStudijaDTO(vs.getId(), vs.getSkracenica(), vs.getPuniNaziv());
    }

    public StudijskiProgramDTO fromStudijskiProgramToDTO(StudijskiProgram sp) {
        if (sp == null) return null;
        StudijskiProgramDTO dto = new StudijskiProgramDTO();
        dto.setId(sp.getId());
        dto.setOznaka(sp.getOznaka());
        dto.setNaziv(sp.getNaziv());
        dto.setGodinaAkreditacije(sp.getGodinaAkreditacije());
        dto.setZvanje(sp.getZvanje());
        dto.setTrajanjeGodina(sp.getTrajanjeGodina());
        dto.setTrajanjeSemestara(sp.getTrajanjeSemestara());
        dto.setUkupnoEspb(sp.getUkupnoEspb());
        dto.setVrstaStudija(fromVrstaStudijaToDTO(sp.getVrstaStudija()));
        return dto;
    }

    public SkolskaGodinaDTO fromSkolskaGodinaToDTO(SkolskaGodina sg) {
        if (sg == null) return null;
        return new SkolskaGodinaDTO(sg.getId(), sg.getGodina(), sg.isAktivna());
    }

    public NastavnikLiteDTO fromNastavnikToLiteDTO(Nastavnik n) {
        if (n == null) return null;
        return new NastavnikLiteDTO(n.getId(), n.getIme(), n.getPrezime(), n.getEmail());
    }

    public PredmetDTO fromPredmetToDTO(Predmet p) {
        if (p == null) return null;
        return new PredmetDTO(
                p.getId(),
                p.getSifra(),
                p.getNaziv(),
                p.getOpis(),
                p.getEspb(),
                p.getStudProgram() != null ? p.getStudProgram().getOznaka() : null
        );
    }

    public DrziPredmetDTO fromDrziPredmetToDTO(DrziPredmet dp) {
        if (dp == null) return null;

        DrziPredmetDTO dto = new DrziPredmetDTO();
        dto.setId(dp.getId());

        if (dp.getPredmet() != null) {
            dto.setPredmetId(dp.getPredmet().getId());
            dto.setPredmetNaziv(dp.getPredmet().getNaziv());
        }

        if (dp.getNastavnik() != null) {
            dto.setNastavnikId(dp.getNastavnik().getId());
            dto.setNastavnikImePrezime(dp.getNastavnik().getIme() + " " + dp.getNastavnik().getPrezime());
        }
        if (dp.getRealizacijaPredmeta() != null) {
            dto.setRealizacijaPredmetaId(dp.getRealizacijaPredmeta().getId());
        }
        dto.setUloga(dp.getUloga() == null ? null : dp.getUloga().name());

        // ako želiš školsku godinu, dodaj polja u DTO pa ovde setuj
        // npr dto.setSkolskaGodinaId(dp.getSkolskaGodina().getId());

        return dto;
    }

    public SlusaPredmetDTO fromSlusaPredmetToDTO(SlusaPredmet sp) {
        if (sp == null) return null;
        SlusaPredmetDTO dto = new SlusaPredmetDTO();
        dto.setId(sp.getId());
        dto.setStudentIndeks(fromStudentIndexToLiteDTO(sp.getStudentIndeks()));
        DrziPredmet prikazanoAngazovanje = sp.getDrziPredmet();
        if (prikazanoAngazovanje == null && sp.getRealizacijaPredmeta() != null
                && sp.getRealizacijaPredmeta().getAngazovanja() != null) {
            prikazanoAngazovanje = sp.getRealizacijaPredmeta().getAngazovanja().stream()
                    .filter(dp -> dp.getUloga() == DrziPredmet.Uloga.NOSILAC)
                    .findFirst()
                    .orElseGet(() -> sp.getRealizacijaPredmeta().getAngazovanja().stream().findFirst().orElse(null));
        }
        dto.setDrziPredmet(fromDrziPredmetToDTO(prikazanoAngazovanje));
        dto.setSkolskaGodina(fromSkolskaGodinaToDTO(sp.getSkolskaGodina()));
        if (sp.getRealizacijaPredmeta() != null) {
            var r = sp.getRealizacijaPredmeta();
            var pp = r.getProgramPredmet();
            dto.setRealizacijaPredmeta(new RealizacijaPredmetaDTO(
                    r.getId(), pp.getId(), pp.getProgram().getId(), pp.getProgram().getOznaka(),
                    pp.getPredmet().getId(), pp.getPredmet().getSifra(), pp.getPredmet().getNaziv(),
                    pp.getGodinaStudija(), pp.getSemestarUGodini(),
                    r.getSkolskaGodina().getId(), r.getSkolskaGodina().getGodina(), r.getStatus().name()
            ));
        }
        return dto;
    }

    public StudentSubjectDTO fromSlusaPredmetToStudentSubjectDTO(SlusaPredmet listening) {
        if (listening == null || listening.getRealizacijaPredmeta() == null
                || listening.getRealizacijaPredmeta().getProgramPredmet() == null) {
            return null;
        }

        var realization = listening.getRealizacijaPredmeta();
        var programSubject = realization.getProgramPredmet();
        var subject = programSubject.getPredmet();
        StudentSubjectDTO dto = new StudentSubjectDTO();
        dto.setListeningId(listening.getId());
        dto.setRealizationId(realization.getId());
        dto.setSubjectId(subject.getId());
        dto.setCode(subject.getSifra());
        dto.setName(subject.getNaziv());
        dto.setDescription(subject.getOpis());
        dto.setEcts(subject.getEspb());
        dto.setStudyYear(programSubject.getGodinaStudija());
        dto.setSemester(programSubject.getSemestarUkupno());
        dto.setProgramCode(programSubject.getProgram().getOznaka());
        dto.setSchoolYear(realization.getSkolskaGodina().getGodina());
        dto.setRealizationStatus(realization.getStatus().name());
        if (realization.getAngazovanja() != null) {
            dto.setInstructors(realization.getAngazovanja().stream()
                    .sorted(Comparator.comparing(dp -> dp.getUloga() == null ? "" : dp.getUloga().name()))
                    .map(this::fromDrziPredmetToDTO)
                    .collect(Collectors.toList()));
        }
        return dto;
    }
}
