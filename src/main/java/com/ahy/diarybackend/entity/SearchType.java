package com.ahy.diarybackend.entity;

/**
 * 다이어리 검색 범위
 */
public enum SearchType {
    TITLE,          // 제목만 검색
    CONTENT,        // 내용만 검색
    TITLE_CONTENT   // 제목 + 내용 검색 (OR 조건)
}
