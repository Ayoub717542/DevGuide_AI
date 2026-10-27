package com.example.DevTutor.service.serviceInterface;

import com.example.DevTutor.dto.QuizResponse;

public interface QuizService {
    QuizResponse generateQuiz(Long conversationId);
}
