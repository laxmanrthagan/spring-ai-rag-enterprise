package com.ragagentic.ai.model;

import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class DocumentMetadata {
    private String documentId;
    private String fileName;
    private String checksum;
    private Long fileSize;
    private Instant uploadedAt;
    private DocumentStatus status;
    private String errorMessage;
}