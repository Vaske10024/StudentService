package org.raflab.studsluzba.model.dtos;

import lombok.Data;

@Data
public class PredmetAdminUpdateRequest {
    private String naziv;
    private String opis;
    private Integer espb;
}
