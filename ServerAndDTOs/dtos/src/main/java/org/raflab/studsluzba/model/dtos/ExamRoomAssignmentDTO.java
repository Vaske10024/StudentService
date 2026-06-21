package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ExamRoomAssignmentDTO {
    private Long id;
    private Long examId;
    private Long roomId;
    private String roomCode;
    private int expectedStudents;
}
