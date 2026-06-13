package org.raflab.studsluzba.model.dtos;
import lombok.Data;
import javax.validation.constraints.*;
import java.math.BigDecimal;
@Data
public class EnrollmentApplicationCreateDTO {
    @NotBlank private String ime;
    @NotBlank private String prezime;
    @NotBlank private String jmbg;
    @Email @NotBlank private String email;
    @NotBlank private String username;
    @NotNull private Long studijskiProgramId;
    @NotNull private Integer godina;
    private BigDecimal tuitionEur = BigDecimal.ZERO;
}
