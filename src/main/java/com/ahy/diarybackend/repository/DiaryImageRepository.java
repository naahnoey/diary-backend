package com.ahy.diarybackend.repository;

import com.ahy.diarybackend.entity.Diary;
import com.ahy.diarybackend.entity.DiaryImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryImageRepository extends JpaRepository<DiaryImage, Long> {

    // 특정 다이어리의 모든 이미지 조회
    List<DiaryImage> findByDiary(Diary diary);

    // 특정 다이어리의 이미지 개수
    long countByDiary(Diary diary);

    // 저장된 파일명으로 조회
    DiaryImage findByStoredFileName(String storedFileName);

}
