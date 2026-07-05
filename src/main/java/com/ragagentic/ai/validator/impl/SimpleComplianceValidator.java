package com.ragagentic.ai.validator.impl;

import com.ragagentic.ai.validator.ResponseValidator;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SimpleComplianceValidator implements ResponseValidator {

    @Override
    public double calculateConfidence(String answer, List<Document> contextDocs) {
        if (answer == null || contextDocs == null || contextDocs.isEmpty()) {
            return 0.0;
        }

        // Simple heuristic check: Verify string alignment match across the retrieved context
        String lowerAnswer = answer.toLowerCase();
        if (lowerAnswer.contains("i don't know") || lowerAnswer.contains("unavailable")) {
            return 1.0;
        }

        long matchedDocs = contextDocs.stream()
                .filter(doc -> doc.getText() != null && containsAnyKeyPhrases(lowerAnswer, doc.getText().toLowerCase()))
                .count();

        return contextDocs.size() > 0 ? (double) matchedDocs / contextDocs.size() : 1.0;
    }

    private boolean containsAnyKeyPhrases(String answer, String context) {
        // Simple contextual overlap rule
        return context.contains(answer.substring(0, Math.min(answer.length(), 15)));
    }
}