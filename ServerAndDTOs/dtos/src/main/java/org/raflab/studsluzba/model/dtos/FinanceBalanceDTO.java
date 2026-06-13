package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FinanceBalanceDTO {
    private BigDecimal balanceEur;
    private BigDecimal debtEur;
    private BigDecimal creditEur;
}
