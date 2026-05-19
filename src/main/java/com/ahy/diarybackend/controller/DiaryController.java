package com.ahy.diarybackend.controller;

import com.ahy.diarybackend.dto.auth.MessageResponse;
import com.ahy.diarybackend.dto.diary.DiaryCreateRequest;
import com.ahy.diarybackend.dto.diary.DiaryResponse;
import com.ahy.diarybackend.service.DiaryService;
import com.ahy.diarybackend.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Diary API", description = "다이어리 관련 API")
@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    @Operation(summary = "다이어리 작성", description = "날짜별로 다이어리 작성")
    @SecurityRequirement(name = "bearerAuth")
    @PostMapping(path = "/post", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> createDiary(
            @RequestPart("diary")
            @Schema(
                    description = "다이어리 작성 요청 JSON",
                    example = """
                        {
                            "diaryDate": "2026-02-22",
                            "title": "테스트",
                            "content": "달력 기능을 구현해 봅시다",
                            "weather": "SUNNY",
                            "tags": ["여행", "서울", "맛집"]
                        }
                        """
            )
            String diaryJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @AuthenticationPrincipal UserDetails userDetails    // 현재 로그인 사용자 식별
    ) {
        try {
            DiaryCreateRequest request = objectMapper.readValue(diaryJson, DiaryCreateRequest.class);

            // 이미지 개수 검증
            if (images != null && images.size() > 5) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("이미지는 최대 5개까지 업로드 가능합니다"));
            }

            DiaryResponse response = diaryService.createDiary(request, images, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 작성에 실패했습니다: " + e.getMessage()));
        }
    }

    // 이미지 조회 - S3 URL로 리다이렉트
    @Operation(summary = "이미지 다운로드", description = "게시글에 업로드한 이미지 서버에 저장")
    @GetMapping("/images/{fileName}")
    public ResponseEntity<Void> downloadImage(@PathVariable String fileName) {
        try {
            String s3Url = fileStorageService.getFilePath(fileName); // S3 URL 반환
            return ResponseEntity.status(HttpStatus.FOUND)
                    .header(HttpHeaders.LOCATION, s3Url)  // S3 URL로 리다이렉트
                    .build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

}
