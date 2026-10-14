package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.service.serviceInterface.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class ChatServiceImpl implements ChatService {
private final ChatClient chatClient;

    public ChatServiceImpl(ChatClient.Builder builder)
            throws IOException {

        String guide = new ClassPathResource("devtutor-guide.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        this.chatClient = builder
                .defaultSystem(guide)
                .build();
    }

    @Override
    public String reply(String message) throws IOException {

        String answer = chatClient.prompt()
                .user(message)
                .call()
                .content();

        if (answer == null || answer.isBlank()) {
            return "Sorry, I couldn't generate an answer. Please try again.";
        }
        return answer;
    }
}