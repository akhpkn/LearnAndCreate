package com.lac.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccessTokenVkResponse {
    private String access_token;
    private String expires_in;
    private String user_id;
    private String email;
}
