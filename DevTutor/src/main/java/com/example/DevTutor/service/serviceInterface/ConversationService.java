package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.ConversationResponse;
import com.example.DevTutor.dto.CreateConversationRequest;
import com.example.DevTutor.dto.UpdateConversationRequest;

import java.util.List;

public interface ConversationService {
    ConversationResponse createConversation(CreateConversationRequest request);
    List<ConversationResponse> getConversations();
    ConversationResponse renameConversation(Long id, UpdateConversationRequest request);
    void deleteConversation(Long id);
}
