package org.raflab.studsluzba.model.finance;

import lombok.Getter;
import lombok.Setter;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity @Getter @Setter
public class TuitionInstallment {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @ManyToOne(optional = false) private TuitionPlan tuitionPlan;
    @Column(nullable = false, precision = 19, scale = 2) private BigDecimal amountEur;
    @Column(nullable = false) private LocalDate dueDate;
    @Column(nullable = false) private int sequenceNumber;
}
