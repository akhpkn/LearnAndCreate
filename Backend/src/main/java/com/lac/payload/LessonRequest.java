package com.lac.payload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import javax.websocket.server.ServerEndpoint;

@AllArgsConstructor
@Getter
@Setter
public class LessonRequest {

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String description;

    @NotBlank
    private String duration;
}
