package com.ragagentic.ai.audit;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Slf4j
@Service
public class AuditService {

    @Data
    @Builder
    public static class AuditLogEvent {
        private String user;
        private String action;
        private int documentsRetrieved;
        private long responseTimeMs;
        private String status;
        private Instant timestamp;
    }

    public void logEvent(AuditLogEvent event) {
        // Structured log output format ideal for Logstash/Splunk collection
        log.info("AUDIT_LOG | User: {} | Action: {} | Docs: {} | Time: {}ms | Status: {}",
                event.getUser(), event.getAction(), event.getDocumentsRetrieved(),
                event.getResponseTimeMs(), event.getStatus());
    }
}