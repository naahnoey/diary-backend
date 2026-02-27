package com.ahy.diarybackend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Weather {

    SUNNY("맑음", "☀️"),
    CLOUDY("흐림", "☁️"),
    RAINY("비", "🌧️"),
    SNOWY("눈", "❄️"),
    WINDY("바람", "💨"),
    FOGGY("안개", "🌫️"),
    STORMY("폭풍", "⛈️");

    private final String description;  // 한글 설명
    private final String emoji;        // 이모지

    Weather(String description, String emoji) {
        this.description = description;
        this.emoji = emoji;
    }

    // JSON 직렬화 시 사용될 값 (enum 이름: SUNNY, CLOUDY 등)
    @JsonValue
    public String getName() {
        return this.name();
    }

    // JSON 역직렬화 시 문자열을 enum으로 변환
    @JsonCreator
    public static Weather fromString(String value) {
        if (value == null) {
            return null;
        }

        // 대소문자 구분 없이 매칭
        for (Weather weather : Weather.values()) {
            if (weather.name().equalsIgnoreCase(value) ||
                    weather.description.equals(value)) {
                return weather;
            }
        }

        throw new IllegalArgumentException("유효하지 않은 날씨 값입니다: " + value);
    }

    // 전체 설명 반환 (이모지 포함)
    public String getFullDescription() {
        return emoji + " " + description;
    }

}
