package org.raflab.studsluzba.e2e;

import org.junit.jupiter.api.Test;
import org.raflab.studsluzba.model.dtos.StudijskiProgramCreateRequest;
import org.raflab.studsluzba.model.dtos.VrstaStudijaRequest;
import org.raflab.studsluzba.repositories.VrstaStudijaRepository;
import org.raflab.studsluzba.security.ApiException;
import org.raflab.studsluzba.services.StudijskiProgramAdminService;
import org.raflab.studsluzba.services.VrstaStudijaAdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class StudyTypeAdminIT {
    @Autowired VrstaStudijaAdminService typeService;
    @Autowired StudijskiProgramAdminService programService;
    @Autowired VrstaStudijaRepository typeRepo;

    @Test
    void studyTypeCrudRejectsDuplicatesAndDeletionWhileInUse() {
        String prefix = "E2E_TYPE_" + UUID.randomUUID().toString().substring(0, 8);
        Long editableId = typeService.create(type(prefix + "_A", "Editable type"));
        typeService.update(editableId, type(prefix + "_B", "Updated type"));
        assertThat(typeRepo.findById(editableId).orElseThrow(AssertionError::new).getPuniNaziv())
                .isEqualTo("Updated type");

        assertThatThrownBy(() -> typeService.create(type(prefix + "_B", "Duplicate")))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("STUDY_TYPE_EXISTS");

        Long usedId = typeService.create(type(prefix + "_USED", "Used type"));
        StudijskiProgramCreateRequest program = new StudijskiProgramCreateRequest();
        program.setOznaka(prefix);
        program.setNaziv("E2E study type deletion protection");
        program.setGodinaAkreditacije(2100);
        program.setZvanje("Engineer");
        program.setTrajanjeGodina(4);
        program.setUkupnoEspb(240);
        program.setVrstaStudijaId(usedId);
        programService.create(program);

        assertThatThrownBy(() -> typeService.delete(usedId))
                .isInstanceOf(ApiException.class)
                .extracting("code").isEqualTo("STUDY_TYPE_IN_USE");

        typeService.delete(editableId);
        assertThat(typeRepo.findById(editableId)).isEmpty();
    }

    private VrstaStudijaRequest type(String code, String name) {
        VrstaStudijaRequest request = new VrstaStudijaRequest();
        request.setSkracenica(code);
        request.setPuniNaziv(name);
        return request;
    }
}
