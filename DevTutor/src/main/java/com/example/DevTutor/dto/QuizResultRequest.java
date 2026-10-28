package com.example.DevTutor.dto;

public record QuizResultRequest(
        String topic,
        int score,
        int total)
{
}
