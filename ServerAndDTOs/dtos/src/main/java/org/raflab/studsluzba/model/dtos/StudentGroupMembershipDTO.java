package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StudentGroupMembershipDTO {
    private Long id;
    private Long groupId;
    private String groupCode;
    private Long indeksId;
}
