package com.lac.service;

import com.lac.dto.CommentDto;
import com.lac.dto.CoursePageDto;
import com.lac.dto.LessonDto;
import com.lac.dto.mapper.EntityToDtoMapper;
import com.lac.model.*;
import com.lac.payload.FeedbackRequest;
import com.lac.repository.*;
import com.lac.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@AllArgsConstructor
public class CourseService {

    private final CommentRepository commentRepository;

    private final CourseRepository courseRepository;

    private final LessonRepository lessonRepository;

    private final ProgressRepository progressRepository;

    private final UserRepository userRepository;

    private final EntityToDtoMapper entityToDtoMapper = new EntityToDtoMapper();

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
                    comment.setCourse(course);
                    commentRepository.save(comment);
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
//        Course course = courseRepository.findByCourseId(courseId);
        return commentRepository.findAllByCourseId(courseId);
//        return new ArrayList<>(course.getComments());
    }

    public List<CommentDto> getAllReviewsByCourseId(Long courseId) {
        List<Comment> comments = commentRepository.findAllByCourseId(courseId);
        List<CommentDto> reviews = new ArrayList<>();
        for (Comment review : comments) {
            CommentDto dto = entityToDtoMapper.commentToDto(review);
            reviews.add(dto);
        }
        return reviews;
    }

    public void addNewCommentToCourse(Comment comment){
        commentRepository.save(comment);
    }

    public boolean addLesson(Long courseId, Lesson lesson) {
        Course course = courseRepository.findByCourseId(courseId);
        if (course != null) {
            lesson.setCourse(course);
            lessonRepository.save(lesson);
            return true;
        }
        return false;
    }

    public List<Lesson> getLessonsByCourseId(Long courseId) {
        return lessonRepository.findAllByCourseId(courseId);
    }

    public List<LessonDto> getAllLessons(Long courseId, UserPrincipal currentUser) {
        List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
        Progress userProgress = progressRepository.findByUserId(currentUser.getUserId());

        List<LessonDto> infos = new ArrayList<>();
        for (Lesson lesson : lessons) {
            LessonDto dto;
            if (userProgress == null || !userProgress.getLessons().contains(lesson))
                dto = entityToDtoMapper.lessonToDto(lesson, false);
            else dto = entityToDtoMapper.lessonToDto(lesson, true);
            infos.add(dto);
        }
        return infos;
    }

    public Lesson getNextLesson(Long courseId, Long lessonId) {
//        Course course = courseRepository.findByCourseId(courseId);
//        if (course != null) {
//            List<Lesson> lessons = new ArrayList<>(course.getLessons());
            List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
            if (lessons == null)
                return null;

            int i = lessons.indexOf(lessonRepository.findByLessonId(lessonId));

            if (i < lessons.size() - 1 && i >= 0)
                return lessons.get(i + 1);

            return null;
//        }
//        return null;
    }

    public Lesson getPreviousLesson(Long courseId, Long lessonId) {
//        Course course = courseRepository.findByCourseId(courseId);
//        if (course != null) {
//            List<Lesson> lessons = new ArrayList<>(course.getLessons());
            List<Lesson> lessons = lessonRepository.findAllByCourseId(courseId);
            if (lessons == null)
                return null;

            int i = lessons.indexOf(lessonRepository.findByLessonId(lessonId));

            if (i > 0 && i <= lessons.size())
                return lessons.get(i - 1);

            return null;
//        }
//        return null;
    }

    public CoursePageDto getCourseInfo(UserPrincipal currentUser, Long courseId) {
        Course course = courseRepository.findByCourseId(courseId);
        boolean subscribed = false, completed = false;
        int lessonsViewed = 0;
        List<Lesson> lessons = lessonRepository.findAllByCourse(course);
        if (currentUser != null) {
            User user = userRepository.findByUserId(currentUser.getUserId());
            subscribed = user.getCourses().contains(course);

            Progress progress = progressRepository.findByUser(user);
            for (Lesson lesson : lessons) {
                if (progress.getLessons().contains(lesson))
                    lessonsViewed++;
            }

            if (lessonsViewed == lessons.size())
                completed = true;
        }
        int reviews = commentRepository.countCommentsByCourseId(courseId);

        return entityToDtoMapper.courseToPageDto(course, subscribed, reviews, lessons.size(),
                lessonsViewed, completed);
    }
}
