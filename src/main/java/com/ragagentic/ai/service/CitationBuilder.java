package com.ragagentic.ai.service;

import com.ragagentic.ai.dto.SourceDocument;
import org.springframework.ai.document.Document;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class CitationBuilder {

    public List<SourceDocument> build(List<Document> docs) {
        if (docs == null) return List.of();

        return docs.stream()
                .map(doc -> SourceDocument.builder()
                        .fileName(doc.getMetadata().get("fileName") != null ? (String) doc.getMetadata().get("fileName") : "UNKNOWN_SOURCE")
                        .page(doc.getMetadata().get("page") != null ? ((Number) doc.getMetadata().get("page")).intValue() : 1)
                        .content(doc.getText())
                        .score(doc.getMetadata().get("distance") != null ? Double.valueOf(doc.getMetadata().get("distance").toString()) : 1.0)
                        .build())
                .toList();
    }
}