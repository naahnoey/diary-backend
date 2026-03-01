package com.ahy.diarybackend.controller;

import com.ahy.diarybackend.dto.diary.DiaryCreateRequest;
import com.ahy.diarybackend.dto.diary.DiaryResponse;
import com.ahy.diarybackend.entity.Weather;
import com.ahy.diarybackend.service.DiaryService;
import com.ahy.diarybackend.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

@WebMvcTest(
        controllers = DiaryController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = WebSecurityConfiguration.class
        )
)
@DisplayName("DiaryController 이미지 업로드 테스트")
class DiaryControllerImageUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DiaryService diaryService;

    @MockitoBean
    private FileStorageService fileStorageService;

    private DiaryCreateRequest diaryRequest;
    private DiaryResponse diaryResponse;
    private MockMultipartFile imageFile1;
    private MockMultipartFile imageFile2;
    private MockMultipartFile diaryJsonFile;

    @BeforeEach
    void setUp() throws Exception {
        // 다이어리 요청 데이터
        diaryRequest = DiaryCreateRequest.builder()
                .diaryDate(LocalDate.of(2026, 1, 22))
                .title("이미지 테스트 작성")
                .content("이미지 업로드 테스트를 작성한다.")
                .weather(Weather.SUNNY)
                .tags(new HashSet<>(Arrays.asList("테스트", "이미지")))
                .build();

        // 다이어리 응답 데이터
        diaryResponse = DiaryResponse.builder()
                .id(1L)
                .diaryDate(LocalDate.of(2026, 1, 22))
                .title("이미지 테스트 작성")
                .content("이미지 업로드 테스트를 작성한다.")
                .weather(Weather.SUNNY)
                .tags(new HashSet<>(Arrays.asList("테스트", "이미지")))
                .images(new ArrayList<>())
                .userId(1L)
                .username("testuser")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        // 이미지 파일 생성 (MockMultipartFile)
        imageFile1 = new MockMultipartFile(
                "images",
                "test-image1.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 1".getBytes()
        );

        imageFile2 = new MockMultipartFile(
                "images",
                "test-image2.jpg",
                MediaType.IMAGE_JPEG_VALUE,
                "test image content 2".getBytes()
        );

        // JSON 데이터를 multipart로 전송하기 위한 파일
        diaryJsonFile = new MockMultipartFile(
                "diary",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(diaryRequest)
        );
    }

}
