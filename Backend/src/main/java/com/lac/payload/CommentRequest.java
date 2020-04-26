package com.lac.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CommentRequest {
    @NotBlank
    @Size(max = 10000)
    private String text;
}
