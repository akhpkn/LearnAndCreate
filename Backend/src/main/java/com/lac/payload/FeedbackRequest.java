package com.lac.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;

@Getter
@Setter
public class FeedbackRequest {

    @Size(max = 10000)
    private String text;

    @Min(1)
    @Max(5)
    private Integer mark;
}
