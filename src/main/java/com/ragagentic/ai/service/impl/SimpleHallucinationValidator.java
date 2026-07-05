package com.ragagentic.ai.service.impl;

import com.ragagentic.ai.dto.ValidationResult;
import com.ragagentic.ai.service.HallucinationValidator;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SimpleHallucinationValidator implements HallucinationValidator {

    @Override
    public ValidationResult validate(String answer, List<Document> context) {
        if (answer == null || answer.isBlank()) {
            return ValidationResult.builder().grounded(true).confidence(1.0).unsupportedStatements(List.of()).build();
        }

        if (context == null || context.isEmpty()) {
            return ValidationResult.builder().grounded(false).confidence(0.0).unsupportedStatements(List.of(answer)).build();
        }

        // 1. Flatten all source pieces into a single verification body
        String contextText = context.stream()
                .map(Document::getText)
                .collect(Collectors.joining(" ")).toLowerCase();

        // 2. Parse sentences using regex boundaries
        String[] sentences = answer.split("(?<=[.!?])\\s+");
        List<String> unsupported = new ArrayList<>();
        int supportedCount = 0;
        int activeSentences = 0;

        for (String sentence : sentences) {
            String cleanSentence = sentence.trim();
            if (cleanSentence.isBlank()) continue;

            activeSentences++;
            // Heuristic check: semantic containment baseline
            if (contextText.contains(cleanSentence.toLowerCase())) {
                supportedCount++;
            } else {
                unsupported.add(cleanSentence);
            }
        }

        double confidence = activeSentences > 0 ? (double) supportedCount / activeSentences : 1.0;

        return ValidationResult.builder()
                .grounded(unsupported.isEmpty())
                .confidence(confidence)
                .unsupportedStatements(unsupported)
                .build();
    }
}