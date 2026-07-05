package com.ragagentic.ai.service;

import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Service;

@Service
public class ModelRouter {

    public OllamaOptions computeRoutingOptions(String question) {
        if (question == null) {
            return OllamaOptions.builder().model("llama3.2:latest").build(); //llama3.3:70b
        }

        // Routing rule logic: Heavy corporate/legal fields route to Llama3
        String query = question.toLowerCase();
        if (query.contains("legal") || query.contains("finance") || query.contains("compliance")) {
            return OllamaOptions.builder()
                    .model("llama3.2:latest") // for now I am using same model due to unavailability of another model
                    .temperature(0.2) // High deterministic correctness
                    .build();
        }

        // Lightweight questions fall back onto Phi3 to conserve local engine processing resource
        return OllamaOptions.builder()
                .model("llama3.2:latest") // for now I am using same model due to unavailability of another model
                .temperature(0.7)
                .build();
    }
}