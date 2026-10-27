package com.example.DevTutor.dto;

import java.util.List;

// A short quiz the AI generates from a conversation's lesson.
public record QuizResponse(
        String topic,
        List<Question> questions) {

    public record Question(
            String question,
            List<String> options,
            int correctIndex,
            String explanation) {
    }
}
