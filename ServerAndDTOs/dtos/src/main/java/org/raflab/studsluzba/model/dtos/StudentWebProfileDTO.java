package org.raflab.studsluzba.model.dtos;

import lombok.Data;


import java.util.List;



import lombok.Data;

import java.util.List;

@Data
public class StudentWebProfileDTO {
    private StudentIndeksResponse aktivanIndeks;

    // DTO umesto entiteta
    private List<SlusaPredmetDTO> slusaPredmete;
}
