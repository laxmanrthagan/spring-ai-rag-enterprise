package com.ragagentic.ai.service.impl;

import com.ragagentic.ai.config.RagProperties;
import com.ragagentic.ai.service.RerankService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SimpleRerankService implements RerankService {

    private final RagProperties ragProperties;

    @Override
    public List<Document> rerank(String question, List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return documents;
        }
        // Fallback pass-through: To be replaced with a proper cross-encoder model later
        return documents.stream()
                .limit(ragProperties.getFinalTopK())
                .toList();
    }
}