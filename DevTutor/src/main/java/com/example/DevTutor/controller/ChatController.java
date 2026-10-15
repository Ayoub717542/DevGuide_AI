package com.example.DevTutor.controller;

import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.service.serviceInterface.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping
    public String reply(
            @RequestParam String conversationId,
            @RequestBody String message) throws IOException {
        return chatService.reply(conversationId,message);
    }

    @PostMapping("/conversations")
    public Conversation createConversation(@RequestParam String title) {
        return chatService.createConversation(title);
    }

}