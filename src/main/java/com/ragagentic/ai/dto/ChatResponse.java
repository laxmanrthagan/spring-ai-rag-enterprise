package com.ragagentic.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatResponse {
    private String answer;
    private List<SourceDocument> sources;
    private int documentsRetrieved;
    private double confidenceScore;
    private long executionTimeMs;
    private boolean validatedGrounded;
}