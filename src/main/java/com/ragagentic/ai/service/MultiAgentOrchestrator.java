package com.ragagentic.ai.service;

import com.ragagentic.ai.agent.CriticAgent;
import com.ragagentic.ai.agent.GraphRagAgent;
import com.ragagentic.ai.agent.PlannerAgent;
import com.ragagentic.ai.agent.ReasoningAgent;
import com.ragagentic.ai.dto.ChatResponse;
import com.ragagentic.ai.dto.CriticEvaluation;
import com.ragagentic.ai.dto.Plan;
import com.ragagentic.ai.dto.SourceDocument;
import com.ragagentic.ai.dto.RetrievalResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MultiAgentOrchestrator {

    private final PlannerAgent plannerAgent;
    private final RetrievalPipeline retrievalPipeline;
    private final GraphRagAgent graphRagAgent;
    private final ReasoningAgent reasoningAgent;
    private final CriticAgent criticAgent;
    private final CitationBuilder citationBuilder;

    public ChatResponse handle(String question, String conversationId, String departmentFilter) {
        long startTime = System.currentTimeMillis();

        // 1. Planning Stage
        Plan plan = plannerAgent.createPlan(question);
        log.info("Execution Plan generated. Steps: {}, UseGraph: {}", plan.getSteps(), plan.isUseGraphRAG());

        // 2. Multi-Source Context Gathering (Hybrid Vector Pipeline + Graph Engine)
        RetrievalResult vectorResult = retrievalPipeline.retrieve(plan.getOptimizedQuery(), departmentFilter);
        List<Document> documents = vectorResult.getDocuments();

        List<String> combinedContextElements = new ArrayList<>();
        documents.stream().map(Document::getText).forEach(combinedContextElements::add);

        plan.setUseGraphRAG(true); // temp for testing the neo4j flow
        if (plan.isUseGraphRAG()) {
            List<String> graphRelations = graphRagAgent.fetchRelations("database"); //pass hardcoded for temp : original plan.getOptimizedQuery()
            combinedContextElements.addAll(graphRelations);
        }

        String aggregatedContext = combinedContextElements.stream().collect(Collectors.joining("\n"));

        // 3. Initial Generation
        String currentAnswer = reasoningAgent.reason(question, aggregatedContext, null);

        // 4. Governance Evaluation & Self-Healing Loop
        CriticEvaluation evaluation = criticAgent.evaluate(currentAnswer, aggregatedContext);
        log.info("Critic audit complete. Validated Grounded: {}, Score: {}", evaluation.isValid(), evaluation.getGroundingScore());

        if (!evaluation.isValid()) {
            log.warn("Hallucination detected by Critic Agent. Executing self-healing cycle...");

            // Re-run the generation step by providing the critic's explicit feedback
            currentAnswer = reasoningAgent.reason(question, aggregatedContext, evaluation.getFeedbackForReasoner());

            // Final validation verification
            evaluation = criticAgent.evaluate(currentAnswer, aggregatedContext);
        }

        // 5. Structure Final Output Package
        List<SourceDocument> sources = citationBuilder.build(documents);
        long totalDuration = System.currentTimeMillis() - startTime;

        return ChatResponse.builder()
                .answer(currentAnswer)
                .sources(sources)
                .documentsRetrieved(sources.size())
                .confidenceScore(evaluation.getGroundingScore())
                .executionTimeMs(totalDuration)
                .validatedGrounded(evaluation.isValid())
                .build();
    }
}