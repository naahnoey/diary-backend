package com.ahy.diarybackend.service;

import com.ahy.diarybackend.dto.diary.DiaryCreateRequest;
import com.ahy.diarybackend.dto.diary.DiaryImageResponse;
import com.ahy.diarybackend.dto.diary.DiaryResponse;
import com.ahy.diarybackend.dto.diary.DiaryUpdateRequest;
import com.ahy.diarybackend.entity.*;
import com.ahy.diarybackend.repository.DiaryRepository;
import com.ahy.diarybackend.repository.TagRepository;
import com.ahy.diarybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final FileStorageService fileStorageService;

    // 다이어리 작성
    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest request, List<MultipartFile> images, String username) throws IOException {
        // 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 해당 날짜에 이미 다이어리가 있는지 확인
        boolean existsForDate = diaryRepository.existsByUserAndDiaryDate(user, request.getDiaryDate());
        if (existsForDate) {
            throw new RuntimeException("해당 날짜에 이미 다이어리가 작성되어 있습니다");
        }

        // 다이어리 생성
        Diary diary = Diary.builder()
                .diaryDate(request.getDiaryDate())
                .title(request.getTitle())
                .content(request.getContent())
                .weather(request.getWeather())
                .user(user)
                .images(new ArrayList<>())
                .tags(new HashSet<>())
                .build();

        // 이미지 처리
        if (images != null && !images.isEmpty()) {
            for (MultipartFile image : images) {
                if (!image.isEmpty()) {
                    DiaryImage diaryImage = saveImage(image, diary);
                    diary.addImage(diaryImage);
                }
            }
        }

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = processTags(request.getTags());
            tags.forEach(diary::addTag);
        }

        Diary savedDiary = diaryRepository.save(diary);

        // 응답 DTO 변환
        return convertToResponse(savedDiary);
    }

    @Transactional
    public DiaryResponse updateDiary(
            Long diaryId,
            DiaryUpdateRequest request,
            List<MultipartFile> newImages,
            String username
    ) throws IOException {
        // 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 다이어리 조회 (본인 소유인지 확인)
        Diary diary = diaryRepository.findByIdAndUser(diaryId, user)
                .orElseThrow(() -> new RuntimeException("다이어리를 찾을 수 없거나 수정 권한이 없습니다."));

        // 날짜가 변경되었다면 중복 체크
        if (!diary.getDiaryDate().equals(request.getDiaryDate())) {
            boolean existsForDate = diaryRepository.existsByUserAndDiaryDate(user, request.getDiaryDate());
            if (existsForDate) {
                throw new RuntimeException("해당 날짜에 이미 다이어리가 작성되어 있습니다.");
            }
        }

        // 기본 정보 수정
        diary.updateContent(
                request.getDiaryDate(),
                request.getTitle(),
                request.getContent(),
                request.getWeather()
        );

        // 태그 갱신 (기존 태그 모두 제거 후 새로 추가)
        diary.clearTags();
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = processTags(request.getTags());
            tags.forEach(diary::addTag);
        }

        // 이미지 삭제 처리
        List<Long> deletedImageIds = request.getDeletedImageIds();

        if (deletedImageIds != null && !deletedImageIds.isEmpty()) {
            List<DiaryImage> imagesToDelete = diary.getImages().stream()
                    .filter(img -> deletedImageIds.contains(img.getId()))
                    .collect(Collectors.toList());

            deleteImage(imagesToDelete, diary);
        }

        // 새 이미지 추가
        if (newImages != null && !newImages.isEmpty()) {
            // 최종 이미지 개수 검증 (기존 유지 + 새로 추가)
            int totalImageCount = diary.getImages().size() + (int) newImages.stream()
                    .filter(f -> !f.isEmpty())
                    .count();
            if (totalImageCount > 5) {
                throw new IllegalArgumentException("이미지는 최대 5개까지 업로드 가능합니다");
            }

            for (MultipartFile image : newImages) {
                if (!image.isEmpty()) {
                    DiaryImage diaryImage = saveImage(image, diary);
                    diary.addImage(diaryImage);
                }
            }
        }

        Diary updatedDiary = diaryRepository.save(diary);

        return convertToResponse(updatedDiary);
    }

    // 다이어리 삭제
    @Transactional
    public void deleteDiary(Long diaryId, String username) throws IOException {
        // 사용자 조회
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        // 다이어리 조회 (본인 소유인지 확인)
        Diary diary = diaryRepository.findByIdAndUser(diaryId, user)
                .orElseThrow(() -> new RuntimeException("다이어리를 찾을 수 없거나 삭제 권한이 없습니다."));

        // 이미지 삭제 처리
        List<DiaryImage> images = diary.getImages();
        if (images != null && !images.isEmpty()) {
            deleteImage(images, diary);
        }

        diaryRepository.delete(diary);
    }

    // 이미지 저장
    private DiaryImage saveImage(MultipartFile file, Diary diary) throws IOException {
        // 이미지 파일 검증
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("이미지 파일만 업로드 가능합니다");
        }

        // 파일 크기 검증 (10MB 제한)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다");
        }

        // 파일 저장
        String storedFileName = fileStorageService.storeFile(file);
        String filePath = fileStorageService.getFilePath(storedFileName);

        return DiaryImage.builder()
                .originalFileName(file.getOriginalFilename())
                .storedFileName(storedFileName)
                .filePath(filePath)
                .fileSize(file.getSize())
                .contentType(file.getContentType())
                .diary(diary)
                .build();
    }

    // 이미지 삭제
    private void deleteImage(List<DiaryImage> deletedImages, Diary diary) throws IOException {
        for (DiaryImage image : deletedImages) {
            // 실제 파일 삭제
            fileStorageService.deleteFile(image.getStoredFileName());
            // 연관관계 제거 (orphanRemoval에 의해 DB에서도 삭제됨)
            diary.removeImage(image);
        }
    }

    // ID로 단건 조회 (상세 조회)
    @Transactional(readOnly = true)
    public DiaryResponse getDiaryById(Long diaryId, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Diary diary = diaryRepository.findByIdAndUser(diaryId, user)
                .orElseThrow(() -> new RuntimeException("다이어리를 찾을 수 없습니다."));

        return convertToResponse(diary);
    }

    // 특정 날짜로 단건 조회 (캘린더에서 날짜 클릭 시)
    @Transactional(readOnly = true)
    public DiaryResponse getDiaryByDate(LocalDate date, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Diary diary = diaryRepository.findByUserAndDiaryDate(user, date)
                .orElseThrow(() -> new RuntimeException("해당 날짜에 작성된 다이어리가 없습니다."));

        return convertToResponse(diary);
    }

    // 특정 월의 다이어리 목록 조회 (캘린더 화면 표시용)
    @Transactional(readOnly = true)
    public List<DiaryResponse> getMonthlyDiaries(int year, int month, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<Diary> diaries = diaryRepository.findByUserAndDiaryDateBetweenOrderByDiaryDateDesc(
                user, startDate, endDate
        );

        return diaries.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    // 전체 다이어리 페이징 목록 조회
    @Transactional(readOnly = true)
    public Page<DiaryResponse> getDiaries(String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Page<Diary> diaries = diaryRepository.findByUserOrderByDiaryDateDesc(user, pageable);

        return diaries.map(this::convertToResponse);
    }

    // 다이어리 검색 (검색 범위를 SearchType으로 지정)
    @Transactional(readOnly = true)
    public Page<DiaryResponse> searchDiaries(SearchType searchType, String keyword, String username, Pageable pageable) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다: " + username));

        Page<Diary> diaries = switch (searchType) {
            case TITLE -> diaryRepository.findByUserAndTitleContainingIgnoreCaseOrderByDiaryDateDesc(
                    user, keyword, pageable);
            case CONTENT -> diaryRepository.findByUserAndContentContainingIgnoreCaseOrderByDiaryDateDesc(
                    user, keyword, pageable);
            case TITLE_CONTENT -> diaryRepository.findByUserAndTitleContainingIgnoreCaseOrContentContainingIgnoreCaseOrderByDiaryDateDesc(
                    user, keyword, keyword, pageable);
        };

        return diaries.map(this::convertToResponse);
    }

    // 태그 처리 - 기존 태그는 재사용, 없으면 새로 생성
    private Set<Tag> processTags(Set<String> tagNames) {
        Set<Tag> tags = new HashSet<>();

        for (String tagName : tagNames) {
            // 태그 이름 정제 (앞뒤 공백 제거, 소문자 변환)
            String cleanedName = tagName.trim().toLowerCase();

            if (cleanedName.isEmpty() || cleanedName.length() > 50) {
                continue;  // 빈 태그나 너무 긴 태그는 무시
            }

            // 기존 태그 찾기 또는 새로 생성
            Tag tag = tagRepository.findByName(cleanedName)
                    .orElseGet(() -> Tag.builder()
                            .name(cleanedName)
                            .diaries(new HashSet<>())
                            .build());

            tags.add(tag);
        }

        return tags;
    }

    // Entity를 Response DTO로 변환
    private DiaryResponse convertToResponse(Diary diary) {
        return DiaryResponse.builder()
                .id(diary.getId())
                .diaryDate(diary.getDiaryDate())
                .title(diary.getTitle())
                .content(diary.getContent())
                .weather(diary.getWeather())
                .weatherDescription(diary.getWeather() != null ? diary.getWeather().getFullDescription() : null)
                .images(diary.getImages().stream()
                        .map(this::convertToImageResponse)
                        .collect(Collectors.toList()))
                .tags(diary.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()))
                .userId(diary.getUser().getId())
                .username(diary.getUser().getUsername())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }

    // DiaryImage를 DiaryImageResponse로 변환
    private DiaryImageResponse convertToImageResponse(DiaryImage image) {
        return DiaryImageResponse.builder()
                .id(image.getId())
                .originalFileName(image.getOriginalFileName())
                .imageUrl(image.getFilePath())
                .fileSize(image.getFileSize())
                .contentType(image.getContentType())
                .uploadDate(image.getUploadDate())
                .build();
    }

}
