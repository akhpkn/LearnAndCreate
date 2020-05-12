package com.lac.payload;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.web.bind.annotation.GetMapping;

@Builder
@Getter
@AllArgsConstructor
public class VkLoginRequest {

    String id;

    String token;

    String email;
}
