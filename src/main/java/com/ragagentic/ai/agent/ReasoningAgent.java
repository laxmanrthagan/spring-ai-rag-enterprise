package com.ragagentic.ai.agent;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReasoningAgent {

    private final ChatClient chatClient;

    public String reason(String question, String context, String criticFeedback) {
        String userPrompt = String.format("""
            Context Information:
            %s
            
            Prior Critic Feedback (if any):
            %s
            
            User Question: %s
            """, context, criticFeedback != null ? criticFeedback : "None", question);

        return chatClient.prompt()
                .system("You are a factual Reasoning Engine. Synthesize an answer using ONLY the context provided.")
                .user(userPrompt)
                .call()
                .content();
    }
}