package com.lac.service;

import com.lac.model.Comment;
import com.lac.model.Course;
import com.lac.model.Lesson;
import com.lac.model.User;
import com.lac.repository.CommentRepository;
import com.lac.repository.CourseRepository;
import com.lac.repository.LessonRepository;
import com.lac.security.UserPrincipal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.AfterDomainEventPublication;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

@Service
@AllArgsConstructor
public class CommentService {

    private final LessonRepository lessonRepository;

    private final CommentRepository commentRepository;

    private final CourseRepository courseRepository;

    public boolean deleteComment(Long userId, Long commentId){
        Comment comment = commentRepository.findCommentByCommentId(commentId);
        if(comment!=null){
            if(comment.getUser().getUserId() == userId) {
                commentRepository.removeCommentByCommentId(commentId);
                return true;
            }
        }
        return false;
    }

    public  boolean updateComment(Long userId, Long commentId, String text){
        Comment comment = commentRepository.findCommentByCommentId(commentId);
        if(comment!=null){
            if(comment.getUser().getUserId() == userId) {
                comment.setText(text);
                comment.setDate(getDate());
                commentRepository.save(comment);
                return true;
            }
        }
        return false;
    }

    public boolean addCommentToCourse(Long courseId, Comment comment){
        Course course = courseRepository.findByCourseId(courseId);
        if( course !=null){
//            course.addComment(comment);
            comment.setCourse(course);
            comment.setDate(getDate());
            commentRepository.save(comment);
            return true;
        }
        return false;
    }

    public boolean addCommentToLesson(Long lessonId, Comment comment){
        Lesson lesson = lessonRepository.findByLessonId(lessonId);
        if( lesson !=null){
            lesson.addComment(comment);
            comment.setDate(getDate());
            commentRepository.save(comment);
            return true;
        }
        return false;
    }

    public String getDate(){
        String months[] = {"января", "февраля", "марта", "апреля", "мая", "июня", "июля", "августа", "сентября",
                "октября", "ноября", "декабря"};
        GregorianCalendar calendar = new GregorianCalendar();
        Integer day = calendar.get(Calendar.DAY_OF_MONTH);
        Integer month = calendar.get(Calendar.MONTH);
        Integer year = calendar.get(Calendar.YEAR);
        return String.format(day + " " + months[month] + " " + year);
    }
}
