package com.ragagentic.ai.agent;

import com.ragagentic.ai.dto.CriticEvaluation;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CriticAgent {

    private final ChatClient chatClient;

    public CriticEvaluation evaluate(String answer, String context) {
        String userPrompt = String.format("""
            Draft Answer to Verify:
            %s
            
            Source Context:
            %s
            """, answer, context);

        return chatClient.prompt()
                .system("""
                    You are a strict Evaluator and Critic. Audit the answer against the context.
                    Verify every statement. Mark valid=false if there are hallucinations or unverified claims.
                    Provide explicit corrective actions in the feedback field.
                    """)
                .user(userPrompt)
                .call()
                .entity(CriticEvaluation.class);
    }
}