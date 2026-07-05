package com.ragagentic.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UploadResponse {
    private String documentId;
    private String fileName;
    private Integer chunksCreated;
    private String message;
}