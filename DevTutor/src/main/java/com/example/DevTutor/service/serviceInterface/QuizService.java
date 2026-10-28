package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.QuizResponse;
import com.example.DevTutor.dto.QuizResultRequest;

public interface QuizService {
    QuizResponse generateQuiz(Long conversationId);
    void saveResult(Long conversationId, QuizResultRequest request);
}
