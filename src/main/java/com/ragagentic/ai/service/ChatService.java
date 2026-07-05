package com.ragagentic.ai.service;

import com.ragagentic.ai.audit.AuditService;
import com.ragagentic.ai.config.RagProperties;
import com.ragagentic.ai.dto.ChatResponse;
import com.ragagentic.ai.dto.SourceDocument;
import com.ragagentic.ai.dto.RetrievalResult;
import com.ragagentic.ai.dto.ValidationResult;
import com.ragagentic.ai.security.PiiMaskingService;
import com.ragagentic.ai.security.PromptInjectionDetector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.document.Document;
import org.springframework.ai.ollama.api.OllamaOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import io.micrometer.tracing.Tracer;
import io.micrometer.tracing.Span;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final VectorStore vectorStore;
    private final RetrievalPipeline retrievalPipeline;

    private final PromptInjectionDetector injectionDetector;
    private final PiiMaskingService piiMaskingService;
    private final HallucinationValidator hallucinationValidator;
    private final CitationBuilder citationBuilder;
    private final AuditService auditService;
    private final RagProperties ragProperties;
    private final CacheService cacheService;
    private final ModelRouter modelRouter;
    private final MetricsService metricsService;
    private final Tracer tracer;

    private static final String SYSTEM_PROMPT = """
            You are an Enterprise AI Assistant operating under strict governance parameters.
            Answer ONLY using the supplied context.
            If information is unavailable or cannot be verified from the context, respond: "I don't know."
            """;

    public ChatResponse ask(String question, String conversationId, String departmentFilter) {
        long startTime = System.currentTimeMillis();
        Span span = tracer.nextSpan()
                .name("Redis Cache")
                .start();
        try (Tracer.SpanInScope scope = tracer.withSpan(span)) {
            // 1. Inbound Security Injection Check
            if (!injectionDetector.isSafe(question)) {
                metricsService.incrementCounter("rag.security.violations");
                auditService.logEvent(AuditService.AuditLogEvent.builder()
                        .user(conversationId).action("ASK_REJECTED").documentsRetrieved(0)
                        .responseTimeMs(System.currentTimeMillis() - startTime).status("PROMPT_INJECTION_DETECTED")
                        .timestamp(Instant.now()).build());
                throw new IllegalArgumentException("Security Violation: Unsafe prompt structure detected.");
            }

            // 2. Redis Caching Interception Layer (Using Collision-Free SHA-256 Key Prefixing)
            String cacheNamespace = "rag:cache:" + (departmentFilter != null ? departmentFilter : "global");
            ChatResponse cachedResponse = cacheService.get(cacheNamespace, question, ChatResponse.class);

            if (cachedResponse != null) {
                metricsService.incrementCounter("rag.cache.hit");
                log.info("Production Cache HIT for context tracking key");

                auditService.logEvent(AuditService.AuditLogEvent.builder()
                        .user(conversationId).action("ASK_COMPLETED_CACHE").documentsRetrieved(cachedResponse.getDocumentsRetrieved())
                        .responseTimeMs(System.currentTimeMillis() - startTime).status("SUCCESS_CACHE_HIT")
                        .timestamp(Instant.now()).build());
                return cachedResponse;
            }
            metricsService.incrementCounter("rag.cache.miss");

            String sanitizedQuestion = piiMaskingService.mask(question);

            // 3. Document Retrieval Matching Performance Bounds
            long retrievalStart = System.currentTimeMillis();
            RetrievalResult retrievalResult = retrievalPipeline.retrieve(sanitizedQuestion, departmentFilter);
            metricsService.recordLatency("rag.pipeline.retrieval.latency", System.currentTimeMillis() - retrievalStart);

            List<Document> contextDocs = retrievalResult.getDocuments();

            // 4. Compute Dynamic Model Routing via ChatOptions
            OllamaOptions routedOptions = modelRouter.computeRoutingOptions(sanitizedQuestion);

            SearchRequest searchRequest = SearchRequest.builder()
                    .query(sanitizedQuestion)
                    .topK(contextDocs.isEmpty() ? 1 : contextDocs.size())
                    .build();

            // 5. Invoke LLM Backend Passing Options Arguments
            long llmStart = System.currentTimeMillis();
            org.springframework.ai.chat.model.ChatResponse response = chatClient.prompt()
                    .system(SYSTEM_PROMPT)
                    .user(sanitizedQuestion)
                    .options(routedOptions) // Dynamic hardware target configuration mapping
                    .advisors(
                            new MessageChatMemoryAdvisor(chatMemory, conversationId, 10),
                            new QuestionAnswerAdvisor(vectorStore, searchRequest)
                    )
                    .call()
                    .chatResponse();

            metricsService.recordLatency("rag.pipeline.llm.latency", System.currentTimeMillis() - llmStart);
            String rawOutput = response.getResult().getOutput().getContent();

            // 6. Grounding, Validation & Compliance
            ValidationResult validationResult = hallucinationValidator.validate(rawOutput, contextDocs);
            List<SourceDocument> sources = citationBuilder.build(contextDocs);
            String finalizedAnswer = piiMaskingService.mask(rawOutput);
            boolean isGrounded = validationResult.isGrounded();

            if (validationResult.getConfidence() < ragProperties.getConfidenceThreshold()) {
                metricsService.incrementCounter("rag.hallucination.blocked");
                finalizedAnswer = "I don't have enough verified information from corporate documents to safely answer this.";
                isGrounded = false;
            }

            long totalDuration = System.currentTimeMillis() - startTime;
            metricsService.recordLatency("rag.pipeline.total.latency", totalDuration);

            ChatResponse finalResponse = ChatResponse.builder()
                    .answer(finalizedAnswer)
                    .sources(sources)
                    .documentsRetrieved(sources.size())
                    .confidenceScore(validationResult.getConfidence())
                    .executionTimeMs(totalDuration)
                    .validatedGrounded(isGrounded)
                    .build();

            // Save cleanly validated items to Cache to prevent serving hallucinations later
            if (isGrounded) {
                cacheService.put(cacheNamespace, question, finalResponse);
            }

            // Log Enterprise Governance Metrics
            auditService.logEvent(AuditService.AuditLogEvent.builder()
                    .user(conversationId).action("ASK_COMPLETED").documentsRetrieved(sources.size())
                    .responseTimeMs(totalDuration).status(isGrounded ? "SUCCESS" : "FALLBACK_TRIGGERED")
                    .timestamp(Instant.now()).build());

            return finalResponse;
        }
        finally {
            span.end();
        }
    }

    /**
     * High-Throughput Streaming Event API Endpoint Hook
     */
    public Flux<String> stream(String question, String conversationId, String departmentFilter) {
        if (!injectionDetector.isSafe(question)) {
            throw new IllegalArgumentException("Security Violation: Unsafe prompt structure detected.");
        }

        String sanitizedQuestion = piiMaskingService.mask(question);
        RetrievalResult retrievalResult = retrievalPipeline.retrieve(sanitizedQuestion, departmentFilter);
        OllamaOptions routedOptions = modelRouter.computeRoutingOptions(sanitizedQuestion);

        SearchRequest searchRequest = SearchRequest.builder()
                .query(sanitizedQuestion)
                .topK(retrievalResult.getDocuments().isEmpty() ? 1 : retrievalResult.getDocuments().size())
                .build();

        return chatClient.prompt()
                .system(SYSTEM_PROMPT)
                .user(sanitizedQuestion)
                .options(routedOptions)
                .advisors(
                        new MessageChatMemoryAdvisor(chatMemory, conversationId, 10),
                        new QuestionAnswerAdvisor(vectorStore, searchRequest)
                )
                .stream()
                .content();
    }
}