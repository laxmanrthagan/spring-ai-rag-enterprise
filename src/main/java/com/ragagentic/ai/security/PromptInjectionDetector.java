package com.ragagentic.ai.security;

public interface PromptInjectionDetector {
    boolean isSafe(String prompt);
}