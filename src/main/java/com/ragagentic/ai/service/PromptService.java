package com.ragagentic.ai.service;

import org.springframework.stereotype.Service;

@Service
public class PromptService {

    public static final String SYSTEM_PROMPT = """
            You are an enterprise assistant.
            Answer ONLY using the supplied context.
            If the answer cannot be found in the context, reply exactly: "I don't know."
            Never invent information. Always be concise.
            """;
}