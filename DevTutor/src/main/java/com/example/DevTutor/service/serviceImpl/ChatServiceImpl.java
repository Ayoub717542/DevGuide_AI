package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.*;
import com.example.DevTutor.entity.Conversation;
import com.example.DevTutor.entity.Message;
import com.example.DevTutor.enums.MessageRole;
import com.example.DevTutor.mapper.ConversationMapper;
import com.example.DevTutor.mapper.MessageMapper;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.MessageRepository;
import com.example.DevTutor.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
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
import org.springframework.web.multipart.MultipartFile;
import com.example.DevTutor.util.MimeTypes;
import org.springframework.ai.content.Media;
import org.springframework.ai.chat.prompt.PromptTemplate;
import com.cloudinary.Cloudinary;
import java.util.Map;
import com.example.DevTutor.tools.DocumentTools;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.tool.ToolCallbackProvider;

@Service
public class ChatServiceImpl implements ChatService {

    private final ChatClient chatClient;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationMapper conversationMapper;
    private final MessageMapper messageMapper;
    private final Cloudinary cloudinary;
    private final UserRepository userRepository;

    public ChatServiceImpl(
            ToolCallbackProvider mcpTools,
            DocumentTools documentTools,
            ChatClient.Builder builder,
            ChatMemory chatMemory,
            VectorStore vectorStore,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ConversationMapper conversationMapper,
            MessageMapper messageMapper, Cloudinary cloudinary,
            UserRepository userRepository
    ) throws IOException {

        this.userRepository = userRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
        this.conversationMapper = conversationMapper;
        this.messageMapper = messageMapper;
        this.cloudinary = cloudinary;

        String guide = new ClassPathResource("devtutor-guide.txt")
                .getContentAsString(StandardCharsets.UTF_8);

        this.chatClient = builder
                .defaultSystem(guide)
                .defaultTools(documentTools).defaultTools(mcpTools)
                .defaultAdvisors(
                        MessageChatMemoryAdvisor.builder(chatMemory).build(),

                        QuestionAnswerAdvisor.builder(vectorStore)
                                .promptTemplate(new PromptTemplate("""
                                Answer the user's question using the conversation
                                and any attached image.

                                Retrieved documents:
                                ---------------------
                                {question_answer_context}
                                ---------------------

                                Use the documents when relevant.
                                If they are empty or irrelevant, answer using
                                the conversation, attached image and your knowledge.
                                Do not invent details about documents or images.
                                If something is unclear, ask for clarification.

                                User question:
                                {query}
                                """))
                                .build()
                )
                .build();
    }

    @Override
    public MessageResponse reply(MessageRequest request, MultipartFile file) {
        if (request.getConversationId() == null
                || request.getContent() == null
                || request.getContent().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Conversation ID and content are required");
        }
        // Only the creator can use a conversation (admins included).
        Conversation conversation = conversationRepository
                .findByIdAndCreatedByEmail(request.getConversationId(), currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));

        Media media = toMedia(file);

        Message userMessage = messageMapper.toEntity(request);
        userMessage.setRole(MessageRole.USER);
        userMessage.setConversation(conversation);
// Upload the image only if one was attached.
        if (media != null) {
            Map<?, ?> uploaded = uploadImage(file);

            userMessage.setImageUrl(
                    uploaded.get("secure_url").toString()
            );

            userMessage.setImagePublicId(
                    uploaded.get("public_id").toString()
            );
        }

        messageRepository.save(userMessage);

        ChatClientResponse aiResponse = chatClient.prompt()
                .user(u -> {
                    u.text(request.getContent());
                    if (media != null) {
                        u.media(media);
                    }
                })
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

        String answer = aiResponse.chatResponse() == null
                ? null
                : aiResponse.chatResponse().getResult().getOutput().getText();

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
        response.setImageUrl(userMessage.getImageUrl());
        return response;
    }
    private Map<?, ?> uploadImage(MultipartFile file) {
        try {
            return cloudinary.uploader().upload(
                    file.getBytes(),
                    Map.of("resource_type", "image")
            );
        } catch (IOException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Could not upload the image",
                    e
            );
        }
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
        conversation.setCreatedBy(userRepository.findByEmail(currentUserEmail())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "User not found")));

        Conversation savedConversation =
                conversationRepository.save(conversation);

        return conversationMapper.toResponse(savedConversation);
    }

    private static String currentUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private static Media toMedia(MultipartFile file) {
        if (file == null) {
            return null;
        }
        if (file.isEmpty()) {
            throw new IllegalArgumentException(
                    "The attached image is empty.");
        }
        String filename = file.getOriginalFilename();

        if (!MimeTypes.isImage(filename)) {
            throw new IllegalArgumentException(
                    "Only PNG, JPEG and WebP images are allowed.");
        }
        try {
            return Media.builder()
                    .mimeType(MimeTypes.ofFilename(filename))
                    .data(file.getBytes())
                    .build();
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Could not read the attached image.");
        }
    }
}