package com.lac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lac.dto.CoursePageDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.HashSet;
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

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id")
    private Image image;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    private Double mark = 0.0;

    @Column(name = "num_marks")
    private Long numMarks = 0L;

    @ManyToMany(mappedBy = "courses", fetch = FetchType.LAZY)
    @JsonIgnore
    private Set<User> users = new HashSet<>();

    public Course(String title, String description, String descriptionLong, String language, String load, Category category) {
        this.title = title;
        this.description = description;
        this.descriptionLong = descriptionLong;
        this.language = language;
        this.load = load;
        this.category = category;
    }
}