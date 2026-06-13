package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UplataDTO {
    private Long id;
    private LocalDate datum;
    private BigDecimal iznosRsd;
    private BigDecimal srednjiKursEur;
    private boolean fallbackKurs;
    private Long indeksId;
}
