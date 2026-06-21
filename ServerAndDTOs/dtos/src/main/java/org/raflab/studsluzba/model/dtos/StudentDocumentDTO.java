package org.raflab.studsluzba.model.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class StudentDocumentDTO {
    private Long id;
    private Long indeksId;
    private Long requestId;
    private String type;
    private String originalName;
    private String contentType;
    private long sizeBytes;
    private LocalDateTime createdAt;
}
