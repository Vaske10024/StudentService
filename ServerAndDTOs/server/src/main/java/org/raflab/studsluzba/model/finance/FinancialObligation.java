package org.raflab.studsluzba.model.finance;

import lombok.Getter;
import lombok.Setter;
import org.raflab.studsluzba.model.StudentIndeks;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity @Getter @Setter
public class FinancialObligation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private StudentIndeks studentIndeks;
    @ManyToOne private TuitionPlan tuitionPlan;
    @Column(nullable = false, length = 80) private String type;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountEur;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal allocatedEur = BigDecimal.ZERO;
    @Column(nullable = false) private LocalDate dueDate;
    @Column(nullable = false) private LocalDateTime createdAt;
    @PrePersist void prePersist() { createdAt = LocalDateTime.now(); }
}
