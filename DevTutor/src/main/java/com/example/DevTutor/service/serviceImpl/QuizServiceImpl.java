package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.QuizResponse;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.service.serviceInterface.QuizService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@Service
public class QuizServiceImpl implements QuizService {

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final ConversationRepository conversationRepository;

    public QuizServiceImpl(ChatClient.Builder builder,
                           ChatMemory chatMemory,
                           ConversationRepository conversationRepository) {
        this.chatClient = builder.build();
        this.chatMemory = chatMemory;
        this.conversationRepository = conversationRepository;
    }

    @Override
    public QuizResponse generateQuiz(Long conversationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        conversationRepository.findByIdAndCreatedByEmail(conversationId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));


        String lesson = chatMemory.get(conversationId.toString()).stream()
                .map(m -> m.getMessageType() + ": " + m.getText())
                .collect(Collectors.joining("\n"));

        if (lesson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chat first, then ask for a quiz.");
        }

        return chatClient.prompt()
                .user("""
                        Write a multiple-choice quiz of 3 to 5 questions
                        about what was taught in this lesson.
                        Each question has 4 options and one correct answer.
                        correctIndex starts at 0.
                        Keep the explanation to one short sentence.

                        Lesson:
                        """ + lesson)
                .call()
                .entity(QuizResponse.class);
    }
}
