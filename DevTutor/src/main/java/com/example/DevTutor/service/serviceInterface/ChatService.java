package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.ConversationResponse;
import com.example.DevTutor.dto.CreateConversationRequest;
import com.example.DevTutor.dto.MessageRequest;
import com.example.DevTutor.dto.MessageResponse;
import com.example.DevTutor.entity.Conversation;

import java.io.IOException;

public interface ChatService {
    MessageResponse reply(MessageRequest request);
    ConversationResponse createConversation(
            CreateConversationRequest request);
}