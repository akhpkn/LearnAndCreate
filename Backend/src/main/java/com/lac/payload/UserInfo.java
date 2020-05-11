package com.lac.payload;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserInfo {

    private final long userId;

    private final String name;

    private final String surname;

    private final String username;

    private final String email;

    private final String imageUrl;

    private final int subscriptions;
}
