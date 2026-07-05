package com.ragagentic.ai.util;

import org.springframework.web.multipart.MultipartFile;
import java.util.Set;

public final class FileValidator {

    private static final Set<String> TYPES = Set.of(
            "application/pdf",
            "text/plain",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    );

    private FileValidator() {}

    public static void validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file or no file provided");
        }
        if (!TYPES.contains(file.getContentType())) {
            throw new IllegalArgumentException("Unsupported file type: " + file.getContentType());
        }
    }
}