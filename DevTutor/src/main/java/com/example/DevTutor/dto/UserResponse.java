package com.example.DevTutor.dto;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private LocalDateTime createdAt;
}
