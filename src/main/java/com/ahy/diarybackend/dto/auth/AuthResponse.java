package com.ahy.diarybackend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 인증 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "인증 응답 DTO")
public class AuthResponse {

    private String token;

    @Builder.Default
    private String type = "Bearer";

    @Schema(description = "회원 일련번호", example = "1")
    private Long id;

    @Schema(description = "회원 ID", example = "testuser")
    private String username;

    @Schema(description = "회원 이메일", example = "test@gmail.com")
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
