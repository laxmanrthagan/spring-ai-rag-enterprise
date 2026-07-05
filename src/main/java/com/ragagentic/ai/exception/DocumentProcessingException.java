package com.ragagentic.ai.exception;

public class DocumentProcessingException extends RuntimeException {
    private final String documentId;

    public DocumentProcessingException(String message, String documentId, Throwable cause) {
        super(message, cause);
        this.documentId = documentId;
    }

    public String getDocumentId() {
        return documentId;
    }
}