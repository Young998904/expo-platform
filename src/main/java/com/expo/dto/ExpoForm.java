package com.expo.dto;

import com.expo.domain.ExpoStatus;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * 박람회 등록/수정 폼. datetime-local 입력을 LocalDateTime으로 바인딩한다.
 */
@Getter
@Setter
public class ExpoForm {

    private String title;
    private String description;
    private String category;
    private String venue;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endAt;

    private int price;
    private int capacity;
    private ExpoStatus status = ExpoStatus.DRAFT;
    private String thumbnailUrl;
}
