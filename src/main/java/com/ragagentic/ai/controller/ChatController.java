package com.ragagentic.ai.controller;

import com.ragagentic.ai.dto.ChatResponse;
import com.ragagentic.ai.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/ask")
    public ChatResponse ask(
            @RequestParam String question,
            @RequestParam String conversationId,
            @RequestParam(required = false) String department) {
        return chatService.ask(question, conversationId, department);
    }

    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> stream(
            @RequestParam String question,
            @RequestParam String conversationId,
            @RequestParam(required = false) String department) {
        return chatService.stream(question, conversationId, department);
    }
}