package com.lac.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "progresses")
@Getter
@Setter
@NoArgsConstructor
public class Progress {

    @Id
    @Column(name = "progress_id")
    private Long progressId;

    @OneToOne
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "progress_lesson",
            joinColumns = @JoinColumn(name = "progress_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_id"))
    private Set<Lesson> lessons = new HashSet<>();

    public boolean addLesson(Lesson lesson) {
        if (lessons.contains(lesson))
            return false;
        lessons.add(lesson);
        return true;
    }
}
