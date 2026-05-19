package com.ahy.diarybackend.dto.diary;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 다이어리 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "다이어리 이미지 업로드 시 응답 DTO")
public class DiaryImageResponse {

    private Long id;
    private String originalFileName;  // 원본 파일명
    private String imageUrl;  // 이미지 접근 URL (예: /api/diaries/images/uuid.jpg)
    private Long fileSize;  // 파일 크기 (bytes)
    private String contentType;  // MIME 타입 (image/jpeg, image/png 등)
    private LocalDateTime uploadDate;  // 업로드 날짜

}
