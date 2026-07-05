package com.ragagentic.ai.dto;

import com.ragagentic.ai.model.DocumentStatus;
import lombok.Builder;
import lombok.Data;
import java.time.Instant;

@Data
@Builder
public class DocumentStatusResponse {
    private String documentId;
    private String fileName;
    private DocumentStatus status;
    private Instant lastUpdated;
    private String message;
}