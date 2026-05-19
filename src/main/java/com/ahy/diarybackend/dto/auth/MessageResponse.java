package com.ahy.diarybackend.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 메시지 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "메시지 응답 DTO")
public class MessageResponse {
    private String message;
}
