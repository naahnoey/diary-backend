package com.ahy.diarybackend.controller;

import com.ahy.diarybackend.dto.auth.MessageResponse;
import com.ahy.diarybackend.dto.diary.DiaryCreateRequest;
import com.ahy.diarybackend.dto.diary.DiaryResponse;
import com.ahy.diarybackend.dto.diary.DiaryUpdateRequest;
import com.ahy.diarybackend.entity.SearchType;
import com.ahy.diarybackend.service.DiaryService;
import com.ahy.diarybackend.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Diary API", description = "다이어리 관련 API")
@RestController
@RequestMapping("/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;
    private final FileStorageService fileStorageService;
    private final ObjectMapper objectMapper;

    // 다이어리 작성
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

    // 다이어리 수정 (이미지 변경 O)
    @Operation(summary = "다이어리 수정", description = "날짜별로 다이어리 작성")
    @SecurityRequirement(name = "bearerAuth")
    @PutMapping(path = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateDiary(
            @PathVariable Long id,
            @RequestPart("diary")
            @Schema(
                    description = "다이어리 수정 요청 JSON",
                    example = """
                        {
                            "diaryDate": "2026-02-22",
                            "title": "테스트",
                            "content": "달력 기능을 구현해 봅시다",
                            "weather": "SUNNY",
                            "tags": ["여행", "서울", "맛집"],
                            "deletedImageIds": [3, 4]
                        }
                        """
            )
            String diaryJson,
            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages,
            @AuthenticationPrincipal UserDetails userDetails    // 현재 로그인 사용자 식별
    ) {
        try {
            DiaryUpdateRequest request = objectMapper.readValue(diaryJson, DiaryUpdateRequest.class);

            // 새로 추가한 이미지 개수 검증
            if (newImages != null && newImages.size() > 5) {
                return ResponseEntity.badRequest()
                        .body(new MessageResponse("이미지는 최대 5개까지 업로드 가능합니다."));
            }

            DiaryResponse response = diaryService.updateDiary(id, request, newImages, userDetails.getUsername());
            return ResponseEntity.status(HttpStatus.OK).body(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    // 다이어리 수정 (이미지 변경 X)
    @PutMapping(path = "/update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> updateDiaryJsonOnly(
            @PathVariable Long id,
            @Schema(
                    description = "다이어리 수정 요청 JSON",
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
            @Valid @RequestBody DiaryUpdateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            DiaryResponse response = diaryService.updateDiary(
                    id, request, null, userDetails.getUsername()
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 수정에 실패했습니다: " + e.getMessage()));
        }
    }

    // 다이어리 삭제
    @Operation(summary = "다이어리 삭제", description = "id로 다이어리 삭제")
    @DeleteMapping(path = "/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> deleteDiary(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            diaryService.deleteDiary(id, userDetails.getUsername());
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 삭제에 실패했습니다: " + e.getMessage()));
        }
    }

    // ID로 다이어리 상세 조회
    @Operation(summary = "다이어리 조회 - id", description = "id로 다이어리 조회")
    @GetMapping("/{id}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getDiaryById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            DiaryResponse response = diaryService.getDiaryById(id, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // 특정 날짜의 다이어리 조회 (캘린더에서 날짜 클릭 시 사용)
    @Operation(summary = "다이어리 조회 - 날짜", description = "날짜로 다이어리 조회")
    @GetMapping("/date/{date}")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getDiaryByDate(
            @Schema(description = "날짜", example = "2026-07-30")
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            DiaryResponse response = diaryService.getDiaryByDate(date, userDetails.getUsername());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new MessageResponse(e.getMessage()));
        }
    }

    // 특정 월의 다이어리 목록 조회 (캘린더 화면 표시용)
    @Operation(summary = "다이어리 월별 목록 조회", description = "월별로 다이어리 목록 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/calendar")
    public ResponseEntity<?> getMonthlyDiaries(
            @RequestParam int year,
            @RequestParam int month,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            List<DiaryResponse> responses = diaryService.getMonthlyDiaries(year, month, userDetails.getUsername());
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // 전체 다이어리 페이징 목록 조회
    @Operation(summary = "전체 다이어리 조회")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping
    public ResponseEntity<?> getDiaries(
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Page<DiaryResponse> responses = diaryService.getDiaries(userDetails.getUsername(), pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 목록 조회에 실패했습니다: " + e.getMessage()));
        }
    }

    // 제목으로 다이어리 검색
    @Operation(summary = "다이어리 검색", description = "제목/내용/제목+내용")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/search")
    public ResponseEntity<?> searchDiaries(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "TITLE") SearchType type,
            @PageableDefault(size = 10) Pageable pageable,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        try {
            Page<DiaryResponse> responses = diaryService.searchDiaries(type, keyword, userDetails.getUsername(), pageable);
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(new MessageResponse("다이어리 검색에 실패했습니다: " + e.getMessage()));
        }
    }

}
