package com.lac.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class CommentInfo {

    private final long commentId;
    private final String text;
    private final long mark;
    private final String date;

    private final long userId;
    private final String userUsername;
    private final String userName;
    private final String userImageUrl;
}
