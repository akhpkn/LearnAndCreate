package com.lac.payload;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
public class CourseInfo {

    private final long courseId;
    private final String title;
    private final String description;
    private final String descriptionLong;
    private final String language;
    private final String load;

    private final String imageUrl;
    private final String introVideoUrl;
    private final String category;
    private final double mark;

    private final long marksNumber;
    private final int subsNumber;
    private final int lessonsNumber;
    private final int reviewsNumber;

    private final boolean subscribed;
}
