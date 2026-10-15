package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.entity.Conversation;

import java.io.IOException;

public interface ChatService {
    String reply(String conversationId,String message) throws IOException;
    Conversation createConversation(String title);
}