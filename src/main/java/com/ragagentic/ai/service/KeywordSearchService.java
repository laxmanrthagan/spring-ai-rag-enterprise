package com.ragagentic.ai.service;

import org.springframework.ai.document.Document;
import java.util.List;

public interface KeywordSearchService {
    List<Document> search(String question);
}