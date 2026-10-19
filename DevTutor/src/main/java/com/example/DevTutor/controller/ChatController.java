package com.example.DevTutor.controller;

import com.example.DevTutor.dto.*;
import com.example.DevTutor.service.serviceInterface.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public MessageResponse reply(
            @RequestParam("conversationId") Long conversationId,
            @RequestParam("content") String content,
            @RequestParam(value = "file", required = false) MultipartFile file) {

        MessageRequest request = new MessageRequest(conversationId, content);

        return chatService.reply(request, file);
    }

    @PostMapping("/conversations")
    public ConversationResponse createConversation(
            @RequestBody CreateConversationRequest request) {

        return chatService.createConversation(request);
    }
}