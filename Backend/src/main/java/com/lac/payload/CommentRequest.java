package com.lac.payload;

import com.lac.model.Comment;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CommentRequest {
    @NotBlank
    @Size(max = 1000)
    private String text;
}
