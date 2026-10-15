package com.example.DevTutor.controller;

import com.example.DevTutor.dto.*;
import com.example.DevTutor.service.serviceInterface.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public MessageResponse reply(@RequestBody MessageRequest request) {
        return chatService.reply(request);
    }

    @PostMapping("/conversations")
    public ConversationResponse createConversation(
            @RequestBody CreateConversationRequest request) {
        return chatService.createConversation(request);
    }
}