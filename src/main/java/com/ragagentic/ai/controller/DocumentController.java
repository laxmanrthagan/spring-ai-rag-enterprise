package com.ragagentic.ai.controller;

import com.ragagentic.ai.dto.DocumentStatusResponse;
import com.ragagentic.ai.dto.UploadResponse;
import com.ragagentic.ai.service.DocumentIngestionService;
import com.ragagentic.ai.service.StatusTrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/documents")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentIngestionService ingestionService;
    private final StatusTrackingService trackingService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadResponse> upload(
            @RequestPart("file") MultipartFile file,
            @RequestParam(value = "department", required = false) String department) {

        UploadResponse response = ingestionService.initiateIngestion(file);

        // Trigger non-blocking async extraction worker pool if it isn't an established duplicate
        if (!response.getMessage().startsWith("DUPLICATE")) {
            ingestionService.processAsync(file, response.getDocumentId(), department);
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(response);
        }

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<DocumentStatusResponse> getStatus(@PathVariable("id") String id) {
        return trackingService.getStatus(id)
                .map(meta -> ResponseEntity.ok(DocumentStatusResponse.builder()
                        .documentId(meta.getDocumentId())
                        .fileName(meta.getFileName())
                        .status(meta.getStatus())
                        .lastUpdated(meta.getUploadedAt())
                        .message(meta.getErrorMessage() != null ? meta.getErrorMessage() : "Execution executing healthily.")
                        .build()))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }
}