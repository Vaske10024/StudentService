package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class LedgerEntryDTO {
    private Long id;
    private String type;
    private BigDecimal amountEur;
    private String description;
    private boolean reversed;
    private LocalDateTime createdAt;
}
