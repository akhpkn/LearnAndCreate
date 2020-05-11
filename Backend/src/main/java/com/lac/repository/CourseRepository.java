package com.lac.repository;

import com.lac.model.Category;
import com.lac.model.Comment;
import com.lac.model.Course;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;


public interface CourseRepository extends JpaRepository<Course, Long> {
    Course findByCourseId(Long courseId);

    boolean existsByTitleAndDescription(String title, String description);

    List<Course> findAllByCategory(Category category);

    List<Course> findAllByCategory(Category category, Sort sort);

    @Query("select c from Course c where c.category = :category " +
            "order by size(c.users) desc")
    List<Course> findByCategoryAndSortedBySubsNumber(@Param("category") Category category);

    List<Course> findAllByTitleContaining(String titleSubstring);

    List<Course> findAllByTitleContaining(String titleSubstring, Sort sort);

    @Query("select c from Course c where c.title like %:title% " +
            "order by size(c.users) desc")
    List<Course> findByTitleContainingAndSortedBySubsNumber(@Param("title") String title);

    List<Course> findAllByCategoryAndTitleContaining(Category category, String titleSubstring);

    List<Course> findAllByCategoryAndTitleContaining(Category category, String titleSubstring, Sort sort);

    @Query("select c from Course c where c.category = :category and c.title like %:title% " +
            "order by size(c.users) desc")
    List<Course> findByCategoryAndTitleAndSortedBySubsNumber(@Param("category") Category category, @Param("title") String title);

    @Query("select c from Course c order by size(c.users) desc ")
    List<Course> findTopPopularCourses(Pageable pageable);

    @Query("select c from Course c order by size(c.users) desc ")
    List<Course> findTopPopularCourses();

    @Query("select c from Course c order by c.courseId")
    List<Course> findAllCourses();
}