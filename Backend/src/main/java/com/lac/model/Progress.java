package com.lac.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "progresses")
@Getter
@Setter
@NoArgsConstructor
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Long progressId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "progress_lesson",
            joinColumns = @JoinColumn(name = "progress_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id"))
    private List<Lesson> lessons = new ArrayList<>();

    public boolean addLesson(Lesson lesson) {
        if (lessons.contains(lesson))
            return false;
        lessons.add(lesson);
        return true;
    }
}
