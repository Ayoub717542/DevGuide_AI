package com.example.DevTutor.dto;

import java.time.LocalDateTime;

public record ReviewResponse(
        String concept,
        int lastScore,
        int lastTotal,
        LocalDateTime nextReview,
        boolean due) {
}
