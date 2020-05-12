package com.lac.dto.mapper;

import com.lac.dto.*;
import com.lac.model.Comment;
import com.lac.model.Course;
import com.lac.model.Lesson;
import com.lac.model.User;

public class EntityToDtoMapper {

    public CourseDto courseToDto(Course course) {
        return CourseDto.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .imageUrl(course.getImage() == null ? "url" : course.getImage().getUrl())
//                .subscribed(subscribed)
                .build();
    }

    public CoursePageDto courseToPageDto(Course course, boolean subscribed, int reviewsNumber, int lessonsNumber) {
        return CoursePageDto.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .description(course.getDescription())
                .category(course.getCategory().getName())
                .descriptionLong(course.getDescriptionLong())
                .language(course.getLanguage())
                .load(course.getLoad())
                .mark(course.getMark())
                .marksNumber(course.getNumMarks())
                .imageUrl(course.getImage() == null ? "url" : course.getImage().getUrl())
                .introVideoId(course.getVideo() == null ? 0 : course.getVideo().getFileId())
                .introVideoUrl(course.getVideo() == null ? "url" : course.getVideo().getUrl())
                .lessonsNumber(lessonsNumber)
                .reviewsNumber(reviewsNumber)
                .subsNumber(course.getUsers().size())
                .subscribed(subscribed)
                .build();
    }

    public CommentDto commentToDto(Comment comment) {
        return CommentDto.builder()
                .commentId(comment.getCommentId())
                .date(comment.getDate())
                .mark(comment.getMark())
                .text(comment.getText())
                .userId(comment.getUser().getUserId())
                .userImageUrl(comment.getUser().getImage() == null ? "url" : comment.getUser().getImage().getUrl())
                .userName(comment.getUser().getName())
                .userUsername(comment.getUser().getUsername())
                .build();
    }

    public LessonDto lessonToDto(Lesson lesson, boolean viewed) {
        return LessonDto.builder()
                .description(lesson.getDescription())
                .duration(lesson.getDuration())
                .lessonId(lesson.getLessonId())
                .title(lesson.getTitle())
                .videoId(lesson.getVideo().getFileId())
                .videoUrl(lesson.getVideo().getUrl())
                .viewed(viewed)
                .build();
    }

    public UserDto userToDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .imageUrl(user.getImage() == null ? "url" : user.getImage().getUrl())
                .name(user.getName())
                .userId(user.getUserId())
                .surname(user.getSurname())
                .subscriptions(user.getCourses().size())
                .username(user.getUsername())
                .build();
    }
}
