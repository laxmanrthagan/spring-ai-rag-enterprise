package com.ragagentic.ai.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SourceDocument {
    private String fileName;
    private Integer page;
    private String content;
    private Double score;
}