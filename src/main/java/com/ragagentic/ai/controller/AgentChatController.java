package com.ragagentic.ai.controller;

import com.ragagentic.ai.dto.ChatResponse;
import com.ragagentic.ai.service.MultiAgentOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/agentic")
@RequiredArgsConstructor
public class AgentChatController {

    private final MultiAgentOrchestrator orchestrator;

    @PostMapping("/ask")
    public ChatResponse ask(
            @RequestParam String question,
            @RequestParam String conversationId,
            @RequestParam(required = false) String department) {
        return orchestrator.handle(question, conversationId, department);
    }
}