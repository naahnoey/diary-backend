package com.ahy.diarybackend.dto.diary;

import com.ahy.diarybackend.entity.Weather;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.Set;

// 다이어리 작성 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "다이어리 작성 요청 DTO")
public class DiaryCreateRequest {

    @Schema(description = "다이어리 날짜", example = "2026-05-18")
    @NotNull(message = "다이어리 날짜는 필수입니다.")
    private LocalDate diaryDate;

    @Schema(description = "제목", example = "다이어리 제목")
    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @Schema(description = "내용", example = "다이어리 내용")
    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    @Schema(description = "날씨", example = "SUNNY")
    private Weather weather;

    @Schema(description = "해시태그", example = "[\"여행\", \"서울\", \"맛집\", \"기분굿\"]")
    @Size(max = 10, message = "태그는 최대 10개까지")
    private Set<String> tags;
    
}
