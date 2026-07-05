package com.ragagentic.ai.service;

import com.ragagentic.ai.dto.ValidationResult;
import org.springframework.ai.document.Document;
import java.util.List;

public interface HallucinationValidator {
    ValidationResult validate(String answer, List<Document> context);
}