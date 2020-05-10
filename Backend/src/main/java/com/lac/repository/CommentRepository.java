package com.lac.repository;

import com.lac.model.Comment;
import com.lac.model.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    Comment findCommentByCommentId(Long commentId);

    void deleteCommentByCommentId(Long commentId);

    @Modifying
    @Transactional
    void removeCommentByCommentId(Long commentId);

    List<Comment> findAllByCourse(Course course);

    @Query("select c from Comment c where c.course.courseId=:courseId order by c.commentId")
    List<Comment> findAllByCourseId(@Param("courseId") Long courseId);

}
