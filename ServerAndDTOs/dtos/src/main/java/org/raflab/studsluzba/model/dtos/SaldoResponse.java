package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SaldoResponse {
    private BigDecimal ukupnoEur;
    private BigDecimal ukupnoRsd;
    private BigDecimal preostaloEur;
    private BigDecimal preostaloRsd;
    private BigDecimal currentEurRate;
    private boolean fallbackRateUsed;
}
