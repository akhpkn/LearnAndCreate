package com.lac.service;

import com.lac.model.Comment;
import com.lac.model.Course;
import com.lac.model.Lesson;
import com.lac.model.User;
import com.lac.payload.FeedbackRequest;
import com.lac.repository.CommentRepository;
import com.lac.repository.CourseRepository;
import com.lac.repository.LessonRepository;
import com.lac.repository.UserRepository;
import com.lac.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class CourseService {

    private final CommentRepository commentRepository;

    private final CourseRepository courseRepository;

    private final LessonRepository lessonRepository;

    private final UserRepository userRepository;

    public boolean addMark(Integer mark, Long courseId){
        Course course = courseRepository.findByCourseId(courseId);
        if(course!=null){
            Double lastMark = course.getMark()==null?0 : course.getMark();
            Long num = course.getNumMarks()==null?0:course.getNumMarks();
            num++;
            Double newMark = (lastMark * (num-1) + mark) / num;
            course.setMark(newMark);
            course.setNumMarks(num);
            courseRepository.save(course);
            return true;
        }
        return false;
    }

    public boolean addFeedback(UserPrincipal currentUser, FeedbackRequest request, Long courseId) {
        if (currentUser != null) {
            Course course = courseRepository.findByCourseId(courseId);
            if (course != null) {
                double lastMark = course.getMark();
                long num = course.getNumMarks() + 1;
                double newMark = (lastMark * (num - 1) + request.getMark()) / num;

                course.setMark(newMark);
                course.setNumMarks(num);

                if (!request.getText().equals("")) {
                    Comment comment = new Comment(request.getText(), request.getMark());
                    comment.setUser(userRepository.findByUserId(currentUser.getUserId()));
                    course.addComment(comment);
                }
                courseRepository.save(course);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean subscribeCourse(UserPrincipal currentUser, Long courseId) {
        if (currentUser != null) {
            User user = userRepository.findByUserId(currentUser.getUserId());
            if (courseRepository.findByCourseId(courseId) != null) {
                user.subscribe(courseRepository.findByCourseId(courseId));
                userRepository.save(user);
                return true;
            }
            return false;
        }
        return false;
    }

    public boolean unsubscribeCourse(UserPrincipal currentUser, Long courseId) {
        if (currentUser != null) {
            User user = userRepository.findByUserId(currentUser.getUserId());
            if (courseRepository.findByCourseId(courseId) != null) {
                user.unsubscribe(courseRepository.findByCourseId(courseId));
                userRepository.save(user);
                return true;
            }
            return false;
        }
        return false;
    }

    public List<Comment> getAllCommentsByCourseId(Long courseId) {
        Course course = courseRepository.findByCourseId(courseId);
        return course.getComments();
    }

    public void addNewCommentToCourse(Comment comment){
        commentRepository.save(comment);
    }

    public boolean addLesson(Long courseId, Lesson lesson) {
        Course course = courseRepository.findByCourseId(courseId);
        if (course != null) {
            course.addLesson(lesson);
            courseRepository.save(course);
            return true;
        }
        return false;
    }

    public List<Lesson> getLessonsByCourseId(Long courseId) {
        Course course = courseRepository.findByCourseId(courseId);
        if (course != null)
            return course.getLessons();
        return new ArrayList<>();
    }

    public Lesson getNextLesson(Long courseId, Long lessonId) {
        Course course = courseRepository.findByCourseId(courseId);
        if (course != null) {
            List<Lesson> lessons = course.getLessons();
            int i = lessons.indexOf(lessonRepository.findByLessonId(lessonId));

            if (i < lessons.size() - 1 && i >= 0)
                return lessons.get(i + 1);

            return null;
        }
        return null;
    }

    public Lesson getPreviousLesson(Long courseId, Long lessonId) {
        Course course = courseRepository.findByCourseId(courseId);
        if (course != null) {
            List<Lesson> lessons = course.getLessons();
            int i = lessons.indexOf(lessonRepository.findByLessonId(lessonId));

            if (i > 0 && i <= lessons.size())
                return lessons.get(i - 1);

            return null;
        }
        return null;
    }
}
