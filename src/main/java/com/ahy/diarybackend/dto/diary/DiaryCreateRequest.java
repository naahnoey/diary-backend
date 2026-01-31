package com.ahy.diarybackend.dto.diary;

import com.ahy.diarybackend.entity.Weather;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// 다이어리 작성 요청 DTO
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryCreateRequest {

    @NotNull(message = "다이어리 날짜는 필수입니다.")
    private LocalDate diaryDate;

    @NotBlank(message = "제목은 필수입니다.")
    @Size(max = 200, message = "제목은 200자 이하여야 합니다.")
    private String title;

    @NotBlank(message = "내용은 필수입니다.")
    private String content;

    private Weather weather;
    
}
