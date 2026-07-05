package com.ragagentic.ai.service;

import org.springframework.ai.document.Document;
import java.util.List;

public interface RerankService {
    List<Document> rerank(String question, List<Document> documents);
}