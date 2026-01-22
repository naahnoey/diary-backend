package com.ahy.diarybackend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인증 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    private Long id;
    private String username;
    private String email;

    public static AuthResponse of(String token, Long id, String username, String email) {
        return AuthResponse.builder()
                .token(token)
                .id(id)
                .username(username)
                .email(email)
                .build();
    }

}
