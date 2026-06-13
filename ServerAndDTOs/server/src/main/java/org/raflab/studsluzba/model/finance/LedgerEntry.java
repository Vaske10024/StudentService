package org.raflab.studsluzba.model.finance;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity @Getter @Setter
public class LedgerEntry {
    public enum Type { CHARGE, PAYMENT, REVERSAL, ADJUSTMENT }
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private StudentIndeks studentIndeks;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private Type type;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountEur;
    @ManyToOne private LedgerEntry reversesEntry;
    @Column(nullable = false) private boolean reversed;
    @Column(length = 500) private String description;
    private Long actorUserId;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
