package org.raflab.studsluzba.model.finance;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter @Setter
@Table(uniqueConstraints = @UniqueConstraint(name = "uk_payment_obligation_allocation", columnNames = {"payment_id", "obligation_id"}))
public class PaymentAllocation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private LedgerEntry payment;
    @ManyToOne(optional = false) private FinancialObligation obligation;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountEur;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
