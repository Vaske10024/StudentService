package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class SystemSettingDTO {
    private Long id;
    private String settingKey;
    private String settingValue;
    private String description;
    private Long updatedByUserId;
    private LocalDateTime updatedAt;
}
