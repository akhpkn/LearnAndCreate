package com.lac.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class LessonInfo {

    private final long lessonId;
    private final String title;
    private final String description;
    private final String duration;

    private final long videoId;
    private final String videoUrl;

    private final boolean viewed;
}
