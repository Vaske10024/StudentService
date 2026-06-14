package org.raflab.studsluzba.services;

import lombok.RequiredArgsConstructor;
import org.raflab.studsluzba.model.VrstaStudija;
import org.raflab.studsluzba.model.dtos.VrstaStudijaRequest;
import org.raflab.studsluzba.repositories.StudijskiProgramRepository;
import org.raflab.studsluzba.repositories.VrstaStudijaRepository;
import org.raflab.studsluzba.security.ApiException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class VrstaStudijaAdminService {
    private final VrstaStudijaRepository typeRepo;
    private final StudijskiProgramRepository programRepo;

    public Long create(VrstaStudijaRequest request) {
        String code = request.getSkracenica().trim();
        if (typeRepo.existsBySkracenicaIgnoreCase(code)) {
            throw ApiException.conflict("STUDY_TYPE_EXISTS", "Vrsta studija sa ovom skracenicom vec postoji.");
        }
        VrstaStudija type = new VrstaStudija();
        apply(type, request);
        return typeRepo.save(type).getId();
    }

    public void update(Long id, VrstaStudijaRequest request) {
        VrstaStudija type = require(id);
        String code = request.getSkracenica().trim();
        if (!type.getSkracenica().equalsIgnoreCase(code) && typeRepo.existsBySkracenicaIgnoreCase(code)) {
            throw ApiException.conflict("STUDY_TYPE_EXISTS", "Vrsta studija sa ovom skracenicom vec postoji.");
        }
        apply(type, request);
        typeRepo.save(type);
    }

    public void delete(Long id) {
        VrstaStudija type = require(id);
        if (programRepo.existsByVrstaStudijaId(id)) {
            throw ApiException.conflict("STUDY_TYPE_IN_USE",
                    "Vrsta studija se ne moze obrisati dok je koristi studijski program.");
        }
        typeRepo.delete(type);
    }

    private VrstaStudija require(Long id) {
        return typeRepo.findById(id).orElseThrow(() -> ApiException.notFound("Vrsta studija ne postoji: " + id));
    }

    private void apply(VrstaStudija type, VrstaStudijaRequest request) {
        type.setSkracenica(request.getSkracenica().trim());
        type.setPuniNaziv(request.getPuniNaziv().trim());
    }
}
