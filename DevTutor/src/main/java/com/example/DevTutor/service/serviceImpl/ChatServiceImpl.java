package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.entity.Message;
import com.example.DevTutor.enums.MessageRole;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.MessageRepository;
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
private final ConversationRepository conversationRepository;
private final MessageRepository messageRepository;


    public ChatServiceImpl(ChatClient.Builder builder, ChatMemory chatMemory, ConversationRepository conversationRepository, MessageRepository messageRepository)
            throws IOException {

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;

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

        Conversation conversation = conversationRepository
                .findById(Long.valueOf(conversationId))
                .orElseThrow(() ->
                        new IllegalArgumentException("Conversation not found"));


        Message userMessage = new Message();
        userMessage.setContent(message);
        userMessage.setRole(MessageRole.USER);
        userMessage.setConversation(conversation);

        messageRepository.save(userMessage);

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

        Message assistantMessage = new Message();
        assistantMessage.setContent(answer);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setConversation(conversation);

        messageRepository.save(assistantMessage);

        return answer;
    }


    @Override
    public Conversation createConversation(String title) {
        Conversation conversation = new Conversation();
        conversation.setTitle(title);

        return conversationRepository.save(conversation);
    }
}