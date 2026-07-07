package com.expo.dto;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

/**
 * VIP 배너 등록 폼.
 */
@Getter
@Setter
public class BannerForm {

    private String title;
    private String imageUrl;
    private String linkUrl;
    private int priority = 100;
    private boolean active = true;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime startAt;

    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime endAt;
}
