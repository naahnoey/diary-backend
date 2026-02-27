package com.ahy.diarybackend.repository;

import com.ahy.diarybackend.entity.Diary;
import com.ahy.diarybackend.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    // 특정 사용자의 모든 다이어리 조회 (최신순)
    List<Diary> findByUserOrderByDiaryDateDesc(User user);

    // 특정 사용자의 다이어리 페이징 조회
    Page<Diary> findByUserOrderByDiaryDateDesc(User user, Pageable pageable);

    // 특정 사용자의 특정 다이어리 조회
    Optional<Diary> findByIdAndUser(Long id, User user);

    // 특정 사용자의 특정 날짜 다이어리 조회
    Optional<Diary> findByUserAndDiaryDate(User user, LocalDate diaryDate);

    // 특정 사용자가 특정 날짜에 다이어리를 작성했는지 확인
    boolean existsByUserAndDiaryDate(User user, LocalDate diaryDate);

    // 특정 사용자의 특정 기간 다이어리 조회 (달력용)
    List<Diary> findByUserAndDiaryDateBetweenOrderByDiaryDateDesc(
            User user,
            LocalDate startDate,
            LocalDate endDate
    );

    // 특정 사용자의 다이어리 개수
    long countByUser(User user);

    // 특정 월의 다이어리가 있는 날짜 조회 (달력 표시용)
    List<Diary> findByUserAndDiaryDateBetween(User user, LocalDate startDate, LocalDate endDate);

}
