package com.ahy.diarybackend.dto.diary;

import com.ahy.diarybackend.entity.Weather;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;

// 다이어리 응답 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryResponse {

    private Long id;
    private LocalDate diaryDate;   // 다이어리 날짜
    private String title;
    private String content;
    private Weather weather;
    private String weatherDescription;
    private Set<String> tags;
    private Long userId;
    private String username;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

}
