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
    @JoinTable(name = "course_image",
            joinColumns = @JoinColumn(name = "course_id "),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    private Image image;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "course_video",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "file_id"))
    private Video video;

    @ManyToOne
    @JoinTable(name = "course_category",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private Category category;

//    @NotBlank
//    @Column(name = "date_creation")
//    private Date date;
//
//    @NotBlank
//    private Integer duration;
//
    private Double mark = 0.0;

    @Column(name = "num_marks")
    private Long numMarks = 0L;

    @ManyToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(name = "user_courses",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id"))
    private Set<User> users = new HashSet<>();

//    @OneToOne(fetch = FetchType.LAZY)
//    @JsonIgnore
//    @JoinTable(name = "creator_user",
//            joinColumns = @JoinColumn(name = "course_id"),
//            inverseJoinColumns = @JoinColumn(name = "user_id"))
//    private User creator = new User();

//    @OneToOne(fetch = FetchType.LAZY)
//    @JsonIgnore
//    @JoinTable(name = "course_video",
//            joinColumns = @JoinColumn(name = "course_id"),
//            inverseJoinColumns = @JoinColumn(name = "file_id"))
//    private Video video = new Video();


    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinTable(name = "course_lessons",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id"))
    private List<Lesson> lessons = new ArrayList<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JsonIgnore
    @JoinTable(name = "course_comments",
            joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "comment_id"))
    private List<Comment> comments = new ArrayList<>();

//    @ManyToMany( fetch =  FetchType.LAZY)
//    @JsonIgnore
//    @JoinTable(name = "course_tags",
//            joinColumns = @JoinColumn(name  = "course_id"),
//            inverseJoinColumns =  @JoinColumn(name = "tag_id"))
//    private Set<Tag> tags = new HashSet<>();// потом заменим на нормальный тег

    public Course(String title, String description, String descriptionLong, String language, String load, Category category) {
        this.title = title;
        this.description = description;
        this.descriptionLong = descriptionLong;
        this.language = language;
        this.load = load;
        this.category = category;
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

    public void deleteComment(Comment comment){
        comments.remove(comment);
    }

    public void addLesson(Lesson lesson) {
        lessons.add(lesson);
    }

    public CourseInfo courseInfo(boolean subscribed) {
        return CourseInfo.builder()
                .courseId(courseId)
                .title(title)
                .description(description)
                .category(category.getName())
                .language(language)
                .load(load)
                .imageUrl(image == null ? "url" : image.getUrl())
                .introVideoUrl(video == null ? "url" : video.getUrl())
                .subsNumber(users.size())
                .reviewsNumber(comments.size())
                .marksNumber(numMarks)
                .mark(mark)
                .lessonsNumber(lessons.size())
                .descriptionLong(descriptionLong)
                .subscribed(subscribed)
                .build();
    }
}