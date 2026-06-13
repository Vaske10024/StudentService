package org.raflab.studsluzba.model.ispiti;


import lombok.Data;
import org.raflab.studsluzba.model.StudentPodaci;

import javax.persistence.*;

@Data
@Entity
public class TokStudija {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;


    @OneToOne
    @JoinColumn(name = "student_podaci_id", unique = true)
    private StudentPodaci studentPodaci;

}
