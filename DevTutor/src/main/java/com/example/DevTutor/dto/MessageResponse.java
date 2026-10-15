package com.example.DevTutor.dto;

import com.example.DevTutor.enums.MessageRole;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private Long id;
    private String content;
    private MessageRole role;
    private LocalDateTime createdAt;
    private Long conversationId;
}
