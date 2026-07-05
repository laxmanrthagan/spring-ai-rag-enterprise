package com.ragagentic.ai.security.impl;

import com.ragagentic.ai.config.SecurityProperties;
import com.ragagentic.ai.security.PiiMaskingService;
import com.ragagentic.ai.security.PromptInjectionDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class RegexSecurityGateway implements PromptInjectionDetector, PiiMaskingService {

    private final SecurityProperties securityProperties;

    // Common enterprise regex rules for PII/Secrets
    private static final Pattern EMAIL_PATTERN = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
    private static final Pattern SSN_PATTERN = Pattern.compile("\\b\\d{3}-\\d{2}-\\d{4}\\b");
    private static final Pattern API_KEY_PATTERN = Pattern.compile("(?i)(aws_secret|openai_key|passwd|password|secret_key)[\\s=:\"']+[a-zA-Z0-9_\\-]{16,}");

    @Override
    public boolean isSafe(String prompt) {
        if (!securityProperties.isEnablePromptInjectionDetection() || prompt == null) {
            return true;
        }
        String lowerPrompt = prompt.toLowerCase();
        return securityProperties.getInjectionKeywords().stream()
                .noneMatch(lowerPrompt::contains);
    }

    @Override
    public String mask(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        String processed = text;
        if (securityProperties.isEnablePiiMasking()) {
            processed = EMAIL_PATTERN.matcher(processed).replaceAll("[EMAIL_REDACTED]");
            processed = SSN_PATTERN.matcher(processed).replaceAll("[SSN_REDACTED]");
        }

        if (securityProperties.isEnableSecretDetection()) {
            processed = API_KEY_PATTERN.matcher(processed).replaceAll("[SECRET_REDACTED]");
        }

        return processed;
    }
}