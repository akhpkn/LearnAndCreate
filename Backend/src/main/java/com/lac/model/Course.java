package com.lac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lac.payload.CourseInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Table(name = "courses")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Course {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "course_id")
    private Long courseId;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String description;

    @NotBlank
    @Size(max = 1000)
    @Column(name = "description_long")
    private String descriptionLong;

    @Size(max = 100)
    private String language;

    @Size(max = 100)
    @Column(name = "course_load")
    private String load;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    private Double mark = 0.0;

    @Column(name = "num_marks")
    private Long numMarks = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(name = "user_courses",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

    public Course(String title, String description, String descriptionLong, String language, String load, Category category) {
        this.title = title;
        this.description = description;
        this.descriptionLong = descriptionLong;
        this.language = language;
        this.load = load;
        this.category = category;
    }

    public CourseInfo courseInfo(boolean subscribed, int reviews, int lessons) {
        return CourseInfo.builder()
                .courseId(courseId)
                .title(title)
                .description(description)
                .category(category.getName())
                .language(language)
                .load(load)
                .imageUrl(image == null ? "url" : image.getUrl())
                .introVideoUrl(video == null ? "url" : video.getUrl())
                .introVideoId(video == null ? 1 : video.getFileId())
                .subsNumber(users.size())
                .reviewsNumber(reviews)
                .marksNumber(numMarks)
                .mark(mark)
                .lessonsNumber(lessons)
                .descriptionLong(descriptionLong)
                .subscribed(subscribed)
                .build();
    }
}