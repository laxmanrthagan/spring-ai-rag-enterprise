package com.ragagentic.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "rag.retrieval")
public class RagProperties {
    private int topK = 10;
    private int finalTopK = 5;
    private double similarityThreshold = 0.75;
    private boolean enableKeywordSearch = true;
    private boolean enableReranking = true;
    private double confidenceThreshold = 0.65;
}