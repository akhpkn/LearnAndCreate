package com.lac.repository;

import com.lac.model.Category;
import com.lac.model.Comment;
import com.lac.model.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;


public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByCourseId(Long courseId);

    boolean existsByTitleAndDescription(String title, String description);

    List<Course> findAllByCategory(Category category);

    List<Course> findAllByTitleContaining(String titleSubstring);

    @Query("select c from Course c order by size(c.users) desc ")
    List<Course> findTopPopularCourses(Pageable pageable);
}