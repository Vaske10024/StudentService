package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SrednjaSkolaDTO {
    private Long id;
    private String naziv;
    private String mesto;
    private String vrsta;
}
