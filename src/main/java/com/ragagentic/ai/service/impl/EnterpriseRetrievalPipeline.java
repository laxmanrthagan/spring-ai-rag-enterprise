package com.ragagentic.ai.service.impl;

import com.ragagentic.ai.config.RagProperties;
import com.ragagentic.ai.dto.RetrievalResult;
import com.ragagentic.ai.service.KeywordSearchService;
import com.ragagentic.ai.service.RerankService;
import com.ragagentic.ai.service.RetrievalPipeline;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnterpriseRetrievalPipeline implements RetrievalPipeline {

    private final VectorStore vectorStore;
    private final KeywordSearchService keywordSearchService;
    private final RerankService rerankService;
    private final RagProperties ragProperties;

    @Override
    public RetrievalResult retrieve(String question, String departmentFilter) {
        long startTime = System.currentTimeMillis();

        // 1. Configure the Vector Search Request via Metadata Filters
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(ragProperties.getTopK())
                .similarityThreshold(ragProperties.getSimilarityThreshold())
                .filterExpression(departmentFilter != null ? "department == '" + departmentFilter + "'" : "")
                .build();

        List<Document> mergedResults;

        // 2. High-Throughput Async Execution via CompletableFuture
        if (ragProperties.isEnableKeywordSearch()) {
            CompletableFuture<List<Document>> vectorTask = CompletableFuture.supplyAsync(() ->
                    vectorStore.similaritySearch(searchRequest)
            );

            CompletableFuture<List<Document>> keywordTask = CompletableFuture.supplyAsync(() ->
                    keywordSearchService.search(question)
            );

            // Run tasks in parallel and merge results upon completion
            mergedResults = vectorTask.thenCombine(keywordTask, this::mergeAndDeduplicate).join();
        } else {
            mergedResults = vectorStore.similaritySearch(searchRequest);
        }

        // 3. Conditional Reranking Stage Execution
        List<Document> finalDocuments = mergedResults;
        if (ragProperties.isEnableReranking()) {
            finalDocuments = rerankService.rerank(question, mergedResults);
        }

        long duration = System.currentTimeMillis() - startTime;
        log.info("Retrieval complete in {}ms. Retrieved {} records.", duration, finalDocuments.size());

        return RetrievalResult.builder()
                .documents(finalDocuments)
                .retrievedCount(finalDocuments.size())
                .retrievalTimeMs(duration)
                .build();
    }

    private List<Document> mergeAndDeduplicate(List<Document> listA, List<Document> listB) {
        List<Document> combined = new ArrayList<>();
        if (listA != null) combined.addAll(listA);
        if (listB != null) combined.addAll(listB);

        // Filter out duplicate chunks cleanly based on Document ID
        return combined.stream()
                .filter(distinctByKey(Document::getId))
                .toList();
    }

    private static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object, Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
    }
}