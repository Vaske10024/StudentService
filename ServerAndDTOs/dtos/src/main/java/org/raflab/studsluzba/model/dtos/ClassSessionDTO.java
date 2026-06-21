package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class ClassSessionDTO {
    private Long id;
    private String title;
    private Long roomId;
    private String roomCode;
    private Long groupId;
    private String groupCode;
    private Long professorId;
    private String professorName;
    private LocalDateTime startsAt;
    private LocalDateTime endsAt;
}
