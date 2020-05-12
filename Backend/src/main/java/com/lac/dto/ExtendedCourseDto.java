package com.lac.dto;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExtendedCourseDto {

    private final long courseId;

    private final String title;

    private final String description;

    private final String imageUrl;

    private final long marksNumber;

    private final double mark;

    private final int subsNumber;

}
