package com.ragagentic.ai.agent;

import com.ragagentic.ai.dto.Plan;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlannerAgent {

    private final ChatClient chatClient;

    public Plan createPlan(String question) {
        return chatClient.prompt().options(computeRoutingOptions())
                .system("""
            You are an Enterprise AI Retrieval Planner.

            Your task is to analyze the user question and create a retrieval execution plan.

            Follow these rules:

            - Generate 2-5 specific execution steps related to the user's question.
            - Each step must describe a real retrieval action.
            - Never use placeholder words.
            - Never return words like:
              step1, step2, actual execution step, optimized search query.

            - Generate optimizedQuery as a concise semantic search phrase
              containing important entities and concepts from the question.

            - Set useGraphRAG=true only when the question requires:
              * relationships between entities
              * ownership information
              * dependency mapping
              * hierarchy traversal
              * connections between systems or people

            - Set useGraphRAG=false for:
              * document lookup
              * configuration lookup
              * factual information retrieval

            Response requirements:
            Return only JSON with exactly these fields:
            
            steps:
              Array of actual retrieval actions.

            useGraphRAG:
              Boolean value.

            optimizedQuery:
              Search query generated from the user's question.

            Do not add explanations.
            Do not add markdown.
            Return ONLY valid JSON.
                            The response MUST end with a closing brace.
            
                            Required JSON:
                            {
                              "steps": [],
                              "useGraphRAG": false,
                              "optimizedQuery": ""
                            }
            """)
                .user(question)
                .call()
                .entity(Plan.class);
    }

    private OllamaOptions computeRoutingOptions() {

        return OllamaOptions.builder()
                .format("json")
                .numPredict(512)
                .build();
    }
}