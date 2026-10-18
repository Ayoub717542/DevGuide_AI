package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.*;
import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.entity.Message;
import com.example.DevTutor.enums.MessageRole;
import com.example.DevTutor.mapper.ConversationMapper;
import com.example.DevTutor.mapper.MessageMapper;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.MessageRepository;
import com.example.DevTutor.service.serviceInterface.ChatService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.ai.document.Document;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;

    public ChatServiceImpl(
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ConversationMapper conversationMapper,
            MessageMapper messageMapper
    ) throws IOException {

        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;

        String guide = new ClassPathResource("devtutor-guide.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        this.chatClient = builder
                .defaultSystem(guide)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),
                        QuestionAnswerAdvisor.builder(vectorStore).build()
                )
                .build();
    }

    @Override
    public MessageResponse reply(MessageRequest request) {

        if (request.getConversationId() == null
                || request.getContent() == null
                || request.getContent().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Conversation ID and content are required");
        }

        Conversation conversation = conversationRepository
                .findById(request.getConversationId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));

        Message userMessage = messageMapper.toEntity(request);
        userMessage.setRole(MessageRole.USER);
        userMessage.setConversation(conversation);

        messageRepository.save(userMessage);

        ChatClientResponse aiResponse = chatClient.prompt()
                .user(request.getContent())
                .advisors(a -> a
                        .param(
                                ChatMemory.CONVERSATION_ID,
                                conversation.getId().toString()
                        )
                        .param(
                                QuestionAnswerAdvisor.FILTER_EXPRESSION,
                                "conversationId == '" + conversation.getId() + "'"
                        )
                )
                .call()
                .chatClientResponse();

        String answer = aiResponse.chatResponse() == null ? null : aiResponse.chatResponse().getResult().getOutput().getText();

        Object retrieved = aiResponse.context()
                .get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);

        List<SourceResponse> sources = new ArrayList<>();

        if (retrieved instanceof List<?> documents) {
            for (Object item : documents) {
                if (item instanceof Document document) {

                    Object filename = document.getMetadata().get("filename");

                    sources.add(new SourceResponse(
                            filename == null ? "Unknown file" : filename.toString(),
                            null,
                            null
                    ));
                }
            }
        }
        if (answer == null || answer.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "The AI returned an empty answer. Please try again.");
        }

        Message assistantMessage = new Message();
        assistantMessage.setContent(answer);
        assistantMessage.setRole(MessageRole.ASSISTANT);
        assistantMessage.setConversation(conversation);

        Message savedAnswer = messageRepository.save(assistantMessage);

        MessageResponse response = messageMapper.toResponse(savedAnswer);
        response.setSources(sources);
        return response;
    }

    @Override
    public ConversationResponse createConversation(
            CreateConversationRequest request) {

        if (request.getTitle() == null || request.getTitle().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Title is required");
        }

        Conversation conversation = conversationMapper.toEntity(request);

        Conversation savedConversation =
                conversationRepository.save(conversation);

        return conversationMapper.toResponse(savedConversation);
    }
}