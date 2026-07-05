package com.ragagentic.ai.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "rag.security")
public class SecurityProperties {
    private boolean enablePromptInjectionDetection = true;
    private boolean enablePiiMasking = true;
    private boolean enableSecretDetection = true;
    private boolean enableResponseValidation = true;

    private List<String> injectionKeywords = List.of(
            "ignore previous instructions",
            "reveal system prompt",
            "bypass security",
            "act as developer",
            "print hidden prompt"
    );
}