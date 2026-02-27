package com.ahy.diarybackend.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "diaries")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 다이어리 날짜 (달력에서 선택한 날짜)
    @Column(name = "diary_date", nullable = false)
    private LocalDate diaryDate;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // 날씨 (enum 사용)
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Weather weather;

    // 해시태그 (다대다 관계)
    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "diary_tags",
            joinColumns = @JoinColumn(name = "diary_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    @Builder.Default
    private Set<Tag> tags = new HashSet<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // 태그 편의 메서드
    public void addTag(Tag tag) {
        this.tags.add(tag);
        tag.getDiaries().add(this);
    }

    public void removeTag(Tag tag) {
        this.tags.remove(tag);
        tag.getDiaries().remove(this);
    }

    public void clearTags() {
        for (Tag tag : new HashSet<>(tags)) {
            removeTag(tag);
        }
    }

}
