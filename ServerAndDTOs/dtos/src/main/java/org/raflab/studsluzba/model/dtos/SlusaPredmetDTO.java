package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlusaPredmetDTO {
    private Long id;
    private StudentIndeksLiteDTO studentIndeks;
    private DrziPredmetDTO drziPredmet;
    private RealizacijaPredmetaDTO realizacijaPredmeta;
    private SkolskaGodinaDTO skolskaGodina;
}
