package com.example.DevTutor.service.serviceImpl;

import com.example.DevTutor.dto.ChallengeDraft;
import com.example.DevTutor.dto.ChallengeResponse;
import com.example.DevTutor.dto.QuizResponse;
import com.example.DevTutor.dto.QuizResultRequest;
import com.example.DevTutor.entity.Challenge;
import com.example.DevTutor.entity.QuizResult;
import com.example.DevTutor.repository.ChallengeRepository;
import com.example.DevTutor.repository.ConversationRepository;
import com.example.DevTutor.repository.QuizResultRepository;
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
    private final QuizResultRepository quizResultRepository;
    private final ChallengeRepository challengeRepository;

    public QuizServiceImpl(ChatClient.Builder builder,
                           ChatMemory chatMemory,
                           ConversationRepository conversationRepository,
                           QuizResultRepository quizResultRepository,
                           ChallengeRepository challengeRepository) {
        this.chatClient = builder.build();
        this.chatMemory = chatMemory;
        this.conversationRepository = conversationRepository;
        this.quizResultRepository = quizResultRepository;
        this.challengeRepository = challengeRepository;
    }

    @Override
    public QuizResponse generateQuiz(Long conversationId) {
        checkOwner(conversationId);
        String lesson = lesson(conversationId);

        return chatClient.prompt()
                .user("""
                        Write a multiple-choice quiz of exactly 3 questions
                        about what was taught in this lesson.
                        Each question has 4 options and one correct answer.
                        correctIndex starts at 0.
                        Keep the explanation to one short sentence.
                        Set topic to a short lowercase concept id, like java-streams or java-generics.
                        Always use the same id for the same concept.

                        Lesson:
                        """ + lesson)
                .call()
                .entity(QuizResponse.class);
    }

    @Override
    public void saveResult(Long conversationId, QuizResultRequest request) {
        String email = checkOwner(conversationId);

        if (request.total() <= 0
                || request.score() < 0
                || request.score() > request.total()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Score must be between 0 and total.");
        }

        QuizResult result = new QuizResult();
        result.setTopic(request.topic());
        result.setScore(request.score());
        result.setTotal(request.total());
        result.setUserEmail(email);
        quizResultRepository.save(result);
    }

    @Override
    public ChallengeResponse createChallenge(Long conversationId) {
        String email = checkOwner(conversationId);
        String lesson = lesson(conversationId);

        ChallengeDraft draft = chatClient.prompt()
                .user("""
                        Create a small Java coding exercise about what was taught in this lesson.
                        The learner writes a class named Solution with one static method.
                        task: explain what to write, including the exact method signature.
                        starterCode: class Solution with that method and a body that only returns a default value.
                        referenceSolution: a complete and correct class Solution.
                        testCode: exactly 3 lines, each like:
                        check("test name", () -> Solution.method(args) == expected);
                        Use .equals for objects and Math.abs(a - b) < 0.001 for doubles.
                        One test must be an edge case. Only use java.util classes.
                        concept: a short lowercase concept id, like java-streams or java-generics.

                        Lesson:
                        """ + lesson)
                .call()
                .entity(ChallengeDraft.class);

        Challenge challenge = new Challenge();
        challenge.setTitle(draft.title());
        challenge.setTask(draft.task());
        challenge.setStarterCode(draft.starterCode());
        challenge.setTestCode(draft.testCode());
        challenge.setReferenceSolution(draft.referenceSolution());
        challenge.setConcept(draft.concept());
        challenge.setUserEmail(email);

        return ChallengeResponse.from(challengeRepository.save(challenge));
    }

    private String lesson(Long conversationId) {
        var messages = chatMemory.get(conversationId.toString());

        String lesson = messages.stream()
                .skip(Math.max(0, messages.size() - 6))
                .map(m -> m.getMessageType() + ": " + m.getText())
                .collect(Collectors.joining("\n"));

        if (lesson.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Chat first, then ask for a quiz or a challenge.");
        }
        return lesson;
    }

    private String checkOwner(Long conversationId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        conversationRepository.findByIdAndCreatedByEmail(conversationId, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Conversation not found"));
        return email;
    }
}
