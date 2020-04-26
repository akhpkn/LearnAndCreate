package com.lac.payload;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
public class CourseRequest {
    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 500)
    private String description;

    @NotBlank
    @Size(max = 1000)
    private String descriptionLong;

    @Size(max = 100)
    private String language;

    @Size(max = 100)
    private String load;

//    @NotBlank
    private String categoryName;
}
