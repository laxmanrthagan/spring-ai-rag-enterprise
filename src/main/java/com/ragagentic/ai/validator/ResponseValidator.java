package com.ragagentic.ai.validator;

import org.springframework.ai.document.Document;
import java.util.List;

public interface ResponseValidator {
    double calculateConfidence(String answer, List<Document> contextDocs);
}