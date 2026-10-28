package com.example.DevTutor.dto;

public record ChallengeResult(
        String status,
        int passed,
        int total,
        String output) {
}
