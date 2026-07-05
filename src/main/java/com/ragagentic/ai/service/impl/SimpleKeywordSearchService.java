package com.ragagentic.ai.service.impl;

import com.ragagentic.ai.service.KeywordSearchService;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class SimpleKeywordSearchService implements KeywordSearchService {
    @Override
    public List<Document> search(String question) {
        // Fallback stub: To be swapped with OpenSearch / Lucene / BM25 later
        return Collections.emptyList();
    }
}