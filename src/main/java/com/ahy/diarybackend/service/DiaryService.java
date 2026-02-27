package com.ahy.diarybackend.service;

import com.ahy.diarybackend.dto.diary.DiaryCreateRequest;
import com.ahy.diarybackend.dto.diary.DiaryResponse;
import com.ahy.diarybackend.entity.Diary;
import com.ahy.diarybackend.entity.Tag;
import com.ahy.diarybackend.entity.User;
import com.ahy.diarybackend.repository.DiaryRepository;
import com.ahy.diarybackend.repository.TagRepository;
import com.ahy.diarybackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;

    // 다이어리 작성
    @Transactional
    public DiaryResponse createDiary(DiaryCreateRequest request, String username) {
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
                .tags(new HashSet<>())
                .build();

        // 태그 처리
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            Set<Tag> tags = processTags(request.getTags());
            tags.forEach(diary::addTag);
        }

        Diary savedDiary = diaryRepository.save(diary);

        // 응답 DTO 변환
        return convertToResponse(savedDiary);
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
                .tags(diary.getTags().stream()
                        .map(Tag::getName)
                        .collect(Collectors.toSet()))
                .userId(diary.getUser().getId())
                .username(diary.getUser().getUsername())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .build();
    }

}
