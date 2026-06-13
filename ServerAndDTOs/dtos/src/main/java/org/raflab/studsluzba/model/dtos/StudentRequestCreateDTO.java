package org.raflab.studsluzba.model.dtos;
import lombok.Data;
import javax.validation.constraints.*;
import java.time.LocalDate;
@Data
public class StudentRequestCreateDTO {
    @NotNull private Long indeksId;
    @NotBlank private String type;
    @NotBlank private String reason;
    private LocalDate requestedFrom;
    private LocalDate requestedTo;
}
