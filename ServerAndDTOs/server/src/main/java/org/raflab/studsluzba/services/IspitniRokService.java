package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.ispiti.IspitniRok;
import org.raflab.studsluzba.model.ispiti.SkolskaGodina;
import org.raflab.studsluzba.model.dtos.IspitniRokCreateRequest;
import org.raflab.studsluzba.model.dtos.IspitniRokUpdateRequest;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.repositories.IspitRepository;
import org.raflab.studsluzba.repositories.IspitniRokRepository;
import org.raflab.studsluzba.repositories.SkolskaGodinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
@Transactional
public class IspitniRokService {

    private final IspitniRokRepository rokRepo;
    private final SkolskaGodinaRepository sgRepo;
    private final IspitRepository ispitRepo;

    public Long create(LocalDate start, LocalDate end, Long sgIdOrNull) {
        SkolskaGodina sg = (sgIdOrNull != null)
                ? sgRepo.findById(sgIdOrNull).orElseThrow(() -> new NoSuchElementException("SG ne postoji: " + sgIdOrNull))
                : sgRepo.findFirstByAktivnaTrue();
        IspitniRok r = new IspitniRok();
        r.setDatumPocetka(start);
        r.setDatumZavrsetka(end);
        r.setSkolskaGodina(sg);
        return rokRepo.save(r).getId();
    }

    public Long create(IspitniRokCreateRequest request) {
        validateWindows(request.getStart(), request.getEnd(), request.getRegistrationStart(),
                request.getRegistrationEnd(), request.getCancellationEnd());
        SkolskaGodina sg = request.getSkolskaGodinaId() != null
                ? sgRepo.findById(request.getSkolskaGodinaId()).orElseThrow(() -> new NoSuchElementException("SG ne postoji: " + request.getSkolskaGodinaId()))
                : sgRepo.findFirstByAktivnaTrue();
        IspitniRok rok = new IspitniRok();
        apply(rok, request.getStart(), request.getEnd(), request.getRegistrationStart(), request.getRegistrationEnd(),
                request.getCancellationEnd(), request.isActive());
        rok.setSkolskaGodina(sg);
        return rokRepo.save(rok).getId();
    }

    public IspitniRok update(Long id, IspitniRokUpdateRequest request) {
        validateWindows(request.getStart(), request.getEnd(), request.getRegistrationStart(),
                request.getRegistrationEnd(), request.getCancellationEnd());
        IspitniRok rok = get(id);
        apply(rok, request.getStart(), request.getEnd(), request.getRegistrationStart(), request.getRegistrationEnd(),
                request.getCancellationEnd(), request.isActive());
        return rokRepo.save(rok);
    }

    private void apply(IspitniRok rok, LocalDate start, LocalDate end, java.time.LocalDateTime registrationStart,
                       java.time.LocalDateTime registrationEnd, java.time.LocalDateTime cancellationEnd, boolean active) {
        rok.setDatumPocetka(start);
        rok.setDatumZavrsetka(end);
        rok.setRegistrationStart(registrationStart);
        rok.setRegistrationEnd(registrationEnd);
        rok.setCancellationEnd(cancellationEnd);
        rok.setActive(active);
    }

    private void validateWindows(LocalDate start, LocalDate end, java.time.LocalDateTime registrationStart,
                                 java.time.LocalDateTime registrationEnd, java.time.LocalDateTime cancellationEnd) {
        if (end.isBefore(start)) throw ApiException.badRequest("Kraj ispitnog roka mora biti posle pocetka.");
        if (registrationStart == null || registrationEnd == null || cancellationEnd == null) {
            throw ApiException.badRequest("Prozori prijave i odjave su obavezni.");
        }
        if (registrationEnd.isBefore(registrationStart)) throw ApiException.badRequest("Kraj prijave mora biti posle pocetka.");
        if (cancellationEnd.isBefore(registrationStart)) throw ApiException.badRequest("Kraj odjave mora biti posle pocetka prijave.");
    }

    @Transactional(readOnly = true)
    public Iterable<IspitniRok> list() { return rokRepo.findAll(); }

    @Transactional(readOnly = true)
    public IspitniRok get(Long id) {
        return rokRepo.findById(id).orElseThrow(() -> new NoSuchElementException("Ispitni rok ne postoji: " + id));
    }
}
