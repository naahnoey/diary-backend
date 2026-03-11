package com.ahy.diarybackend.controller;

import com.ahy.diarybackend.dto.diary.DiaryCreateRequest;
import com.ahy.diarybackend.dto.diary.DiaryResponse;
import com.ahy.diarybackend.entity.Weather;
import com.ahy.diarybackend.security.JwtUtil;
import com.ahy.diarybackend.service.DiaryService;
import com.ahy.diarybackend.service.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = DiaryController.class)
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("DiaryController 이미지 업로드 테스트")
class DiaryControllerImageUploadTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DiaryService diaryService;

    @MockBean
    private FileStorageService fileStorageService;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private UserDetailsService userDetailsService;

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
        String diaryJson = objectMapper.writeValueAsString(diaryRequest);
        diaryJsonFile = new MockMultipartFile(
                "diary",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                diaryJson.getBytes()
        );
    }

    @Test
    @WithMockUser
    @DisplayName("이미지 포함 다이어리 작성 성공")
    void createDiary_WithImages_Success() throws Exception {
        // given
        when(diaryService.createDiary(any(), anyList(), eq("user")))
                .thenReturn(diaryResponse);

        // when & then
        mockMvc.perform(multipart("/diaries/post")
                        .file(diaryJsonFile)
                        .file(imageFile1)
                        .file(imageFile2)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("이미지 테스트 작성"))
                .andExpect(jsonPath("$.weather").value("SUNNY"))
                .andExpect(jsonPath("$.tags").isArray());
    }

    @Test
    @WithMockUser
    @DisplayName("이미지 없이 다이어리 작성 성공")
    void createDiary_WithoutImages_Success() throws Exception {
        // given
        when(diaryService.createDiary(any(), any(), anyString()))
                .thenReturn(diaryResponse);

        // when & then
        mockMvc.perform(multipart("/diaries/post")
                        .file(diaryJsonFile)
                        .with(csrf())
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("이미지 테스트 작성"));
    }

}
