package com.ragagentic.ai.service;

import com.ragagentic.ai.dto.UploadResponse;
import com.ragagentic.ai.exception.DocumentProcessingException;
import com.ragagentic.ai.model.DocumentMetadata;
import com.ragagentic.ai.model.DocumentStatus;
import com.ragagentic.ai.util.FileValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.TextReader;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.tika.TikaDocumentReader;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.core.io.InputStreamResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DocumentIngestionService {

    private final VectorStore vectorStore;
    private final TokenTextSplitter splitter;
    private final StatusTrackingService trackingService;

    /**
     * Step 1: Synchronous Entrypoint validating duplicates immediately
     */
    public UploadResponse initiateIngestion(MultipartFile file) {
        FileValidator.validate(file);

        try {
            String checksum = calculateChecksum(file.getBytes());
            Optional<String> existingId = trackingService.findIdByChecksum(checksum);

            if (existingId.isPresent()) {
                return UploadResponse.builder()
                        .documentId(existingId.get())
                        .fileName(file.getOriginalFilename())
                        .message("DUPLICATE: File already fully indexed and cached.")
                        .build();
            }

            String documentId = UUID.randomUUID().toString();

            DocumentMetadata meta = DocumentMetadata.builder()
                    .documentId(documentId)
                    .fileName(file.getOriginalFilename())
                    .checksum(checksum)
                    .fileSize(file.getSize())
                    .uploadedAt(Instant.now())
                    .status(DocumentStatus.UPLOADED)
                    .build();
            trackingService.save(meta);

            return UploadResponse.builder()
                    .documentId(documentId)
                    .fileName(file.getOriginalFilename())
                    .message("File accepted for background structural ingestion execution.")
                    .build();

        } catch (IOException | NoSuchAlgorithmException e) {
            throw new IllegalArgumentException("Failed early processing validation cycles", e);
        }
    }

    /**
     * Step 2: Asynchronous Non-blocking batch processing pipeline
     */
    @Async("ingestionTaskExecutor")
    public void processAsync(MultipartFile file, String documentId, String department) {
        try {
            trackingService.updateStatus(documentId, DocumentStatus.VALIDATING);
            byte[] fileBytes = file.getBytes();

            trackingService.updateStatus(documentId, DocumentStatus.PROCESSING);
            List<Document> documents = readDocument(fileBytes, file.getOriginalFilename());
            List<Document> chunks = splitter.apply(documents);

            trackingService.updateStatus(documentId, DocumentStatus.EMBEDDING);
            String timestamp = Instant.now().toString();

            chunks.forEach(doc -> {
                doc.getMetadata().put("documentId", documentId);
                doc.getMetadata().put("fileName", file.getOriginalFilename());
                doc.getMetadata().put("uploadedAt", timestamp);
                doc.getMetadata().put("status", DocumentStatus.STORED.name());
                if (department != null) {
                    doc.getMetadata().put("department", department);
                }
            });

            trackingService.updateStatus(documentId, DocumentStatus.STORED);
            // Batch insertion occurs inside the Spring AI implementation layout
            vectorStore.add(chunks);

            trackingService.updateStatus(documentId, DocumentStatus.COMPLETED);

        } catch (Exception e) {
            trackingService.updateFailure(documentId, e.getMessage());
            throw new DocumentProcessingException("Pipeline failure executing background document indexing routines", documentId, e);
        }
    }

    private List<Document> readDocument(byte[] content, String name) {
        InputStreamResource resource = new InputStreamResource(new java.io.ByteArrayInputStream(content));
        if (name != null && name.toLowerCase().endsWith(".pdf")) {
            return new PagePdfDocumentReader(resource).get();
        } else if (name != null && (name.toLowerCase().endsWith(".docx") || name.toLowerCase().endsWith(".doc"))) {
            return new TikaDocumentReader(resource).get();
        } else {
            return new TextReader(resource).get();
        }
    }

    private String calculateChecksum(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(bytes);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}