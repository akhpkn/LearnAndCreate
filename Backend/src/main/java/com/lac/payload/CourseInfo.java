package com.lac.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CourseInfo {

    private final long courseId;
    private final String title;
    private final String description;
    private final String descriptionLong;

    private final String imageUrl;
    private final String category;
    private final double mark;

    private final long marksNumber;
    private final int subsNumber;
    private final int lessonsNumber;
    private final int reviewsNumber;

}
