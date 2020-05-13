package com.lac.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClientVkRequest {
    private String client_id;
    private String client_secret;
    private String redirect_uri;
    private  String code;
}
