package com.lac.repository;

import com.lac.model.Course;
import com.lac.model.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    Lesson findByLessonId(Long lessonId);

    List<Lesson> findAllByCourse(Course course);

    @Query("select l from Lesson l where l.course.courseId=:courseId order by l.lessonId")
    List<Lesson> findAllByCourseId(@Param("courseId") Long courseId);
}
