package com.ragagentic.ai.service;

import com.ragagentic.ai.model.DocumentMetadata;
import com.ragagentic.ai.model.DocumentStatus;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StatusTrackingService {
    // Thread-safe repository cache substitution layer
    private final Map<String, DocumentMetadata> statusDb = new ConcurrentHashMap<>();
    private final Map<String, String> checksumDb = new ConcurrentHashMap<>();

    public void save(DocumentMetadata meta) {
        statusDb.put(meta.getDocumentId(), meta);
        if (meta.getChecksum() != null) {
            checksumDb.put(meta.getChecksum(), meta.getDocumentId());
        }
    }

    public void updateStatus(String documentId, DocumentStatus status) {
        DocumentMetadata current = statusDb.get(documentId);
        if (current != null) {
            current.setStatus(status);
            current.setUploadedAt(Instant.now());
        }
    }

    public void updateFailure(String documentId, String errorReason) {
        DocumentMetadata current = statusDb.get(documentId);
        if (current != null) {
            current.setStatus(DocumentStatus.FAILED);
            current.setErrorMessage(errorReason);
            current.setUploadedAt(Instant.now());
        }
    }

    public Optional<DocumentMetadata> getStatus(String documentId) {
        return Optional.ofNullable(statusDb.get(documentId));
    }

    public Optional<String> findIdByChecksum(String checksum) {
        return Optional.ofNullable(checksumDb.get(checksum));
    }
}