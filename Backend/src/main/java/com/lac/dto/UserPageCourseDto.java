package com.lac.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserPageCourseDto {

    private final long courseId;

    private final String title;

    private final String description;

    private final String imageUrl;

    private final int lessonsNumber;

    private final int lessonsViewed;

    private final boolean completed;

}
