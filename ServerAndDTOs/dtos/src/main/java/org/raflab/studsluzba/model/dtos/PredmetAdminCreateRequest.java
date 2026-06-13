package org.raflab.studsluzba.model.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;



@Data
public class PredmetAdminCreateRequest {
    @NotBlank
    private String sifra;
    @NotBlank private String naziv;
    private String opis;
    private Integer espb;
    private Long programId; // opcionalno
    private boolean obavezan = true;
    private Integer godinaStudija;
    private Integer semestarUGodini;
}
