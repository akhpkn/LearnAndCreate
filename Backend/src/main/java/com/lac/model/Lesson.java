package com.lac.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lac.payload.LessonInfo;
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

@Table(name = "lessons")
@Entity
@Getter
@Setter
@NoArgsConstructor
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "lesson_id")
    private Long lessonId;

    @NotBlank
    @Size(max = 100)
    private String title;

    @NotBlank
    @Size(max = 10000)
    private String description;

   @NotBlank
   private String duration;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "video_id")
    private Video video;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id")
    private Course course;

    @OneToMany(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinTable(name = "lesson_comments",
            joinColumns = @JoinColumn(name = "lesson_id"),
            inverseJoinColumns = @JoinColumn(name = "comment_id"))
    private Set<Comment> comments = new HashSet<>();

    public Lesson(Long lessonId, String title, String description){
        this.lessonId = lessonId;
        this.title = title;
        this.description = description;
    }

    public void addComment(Comment comment){
        comments.add(comment);
    }

    public void deleteComment(Comment comment){
        comments.remove(comment);
    }

    public LessonInfo lessonInfo(boolean viewed) {
        return LessonInfo.builder()
                .description(description)
                .duration(duration)
                .lessonId(lessonId)
                .title(title)
                .videoId(video == null ? 1 : video.getFileId())
                .videoUrl(video == null ? "url" : video.getUrl())
                .viewed(viewed)
                .build();
    }
}
