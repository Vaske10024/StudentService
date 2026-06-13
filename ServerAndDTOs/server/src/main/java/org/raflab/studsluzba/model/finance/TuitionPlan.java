package org.raflab.studsluzba.model.finance;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class TuitionPlan {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private StudentIndeks studentIndeks;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 32) private FinancingType financingType;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal totalEur;
    @Column(nullable = false) private boolean locked;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
