package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.service.serviceInterface.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ChatServiceImpl implements ChatService {
private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient.Builder builder, ChatMemory chatMemory)
            throws IOException {
        String guide = new ClassPathResource("devtutor-guide.txt")
                .getContentAsString(StandardCharsets.UTF_8);
        this.chatClient = builder
                .defaultSystem(guide)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build()
                )
                .build();
    }

    @Override
    public String reply(String conversationId,String message) throws IOException {
        String answer = chatClient.prompt()
                .user(message)
                .advisors(advisorSpec ->
                        advisorSpec.param(
                                ChatMemory.CONVERSATION_ID, conversationId
                        )
                )
                .call()
                .content();
        if (answer == null || answer.isBlank()) {
            return "Sorry, I couldn't generate an answer. Please try again.";
        }
        return answer;
    }
}