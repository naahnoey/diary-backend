package com.ahy.diarybackend.repository;

import com.ahy.diarybackend.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> findById(Long id);

    // 태그 이름으로 조회
    Optional<Tag> findByName(String name);

    // 태그 이름 목록으로 조회
    Set<Tag> findByNameIn(Set<String> names);

    // 태그 이름 존재 여부 확인
    boolean existsByName(String name);

}
