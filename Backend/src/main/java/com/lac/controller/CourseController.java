package com.lac.controller;


import com.lac.dto.CommentDto;
import com.lac.dto.CoursePageDto;
import com.lac.dto.LessonDto;
import com.lac.model.*;
import com.lac.payload.*;
import com.lac.repository.CommentRepository;
import com.lac.repository.CourseRepository;
import com.lac.repository.UserRepository;
import com.lac.security.CurrentUser;
import com.lac.security.UserPrincipal;
import com.lac.service.CommentService;
import com.lac.service.CourseService;
import com.lac.service.ImageService;
import com.lac.service.VideoService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("api/course/")
@AllArgsConstructor
public class CourseController {

    private final CommentService commentService;

    private final CourseService courseService;

    private final CommentRepository commentRepository;

    private final UserRepository userRepository;

    private final ImageService imageService;

    private final VideoService videoService;

    private final CourseRepository courseRepository;

    @GetMapping("{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Course> getCourseById(@PathVariable("courseId") Long courseId){
        Course course = courseRepository.findByCourseId(courseId);
        if(course !=null){
            return new ResponseEntity<>(course, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping("{courseId}/mark")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Void> addMark(@CurrentUser UserPrincipal currentUser,
                                        @Valid @RequestBody MarkRequest request,
                                        @PathVariable("courseId") Long courseId){
        if(courseService.addMark(request.getMark(), courseId)) return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("{courseId}/comment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Comment>> getAllCourseComments(@PathVariable("courseId") Long courseId) {
        List<Comment> comments = courseService.getAllCommentsByCourseId(courseId);
        comments.sort(Comparator.comparingLong(Comment::getCommentId).reversed());
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @GetMapping("{courseId}/reviews")
    public ResponseEntity<List<CommentDto>> getAllCourseReviews(@PathVariable("courseId") Long courseId) {
        List<CommentDto> reviews = courseService.getAllReviewsByCourseId(courseId);
//        reviews.sort(Comparator.comparingLong(CommentInfo::getCommentId).reversed());
        return new ResponseEntity<>(reviews, HttpStatus.OK);
    }

//    @PostMapping("{courseId}/comment")
//    @PreAuthorize("hasRole('USER')")
//    public ResponseEntity<?> addCommentToCourse(@CurrentUser UserPrincipal currentUser,
//                                                @Valid @RequestBody CommentRequest request,
//                                                @PathVariable("courseId") Long courseId) {
//        Comment comment = new Comment(request.getText());
//        comment.setUser(userRepository.findByUserId(currentUser.getUserId()));
//        boolean flag = commentService.addCommentToCourse(courseId, comment);
//        if (!flag)
//            return new ResponseEntity<>(HttpStatus.CONFLICT);
//        return new ResponseEntity<>(HttpStatus.CREATED);
//    }

    @PostMapping("{courseId}/feedback")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addFeedback(@CurrentUser UserPrincipal currentUser,
                                         @Valid @RequestBody FeedbackRequest request,
                                         @PathVariable("courseId") Long courseId) {
        boolean flag = courseService.addFeedback(currentUser, request, courseId);
        if (flag)
            return new ResponseEntity<>(new ApiResponse(true,"Отзыв оставлен!"), HttpStatus.OK);
        return new ResponseEntity<>(
                new ApiResponse(false, "Несуществующий курс или пользователь!"),
                HttpStatus.BAD_REQUEST);
    }

    @PostMapping("/{courseId}/image")
    public UploadFileResponse setCourseImage(@RequestParam(name = "file") MultipartFile file,
                                             @PathVariable("courseId") Long courseId) throws IOException {
        Course course = courseRepository.findByCourseId(courseId);
        if (course.getImage() != null)
            imageService.deleteImage(course.getImage());

        Image image = imageService.store(file);
        course.setImage(image);
        courseRepository.save(course);

        return new UploadFileResponse(image.getUrl(), image.getType(), file.getSize());
    }

    @PostMapping("/{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> subscribeCourse(@CurrentUser UserPrincipal currentUser,
                                                @PathVariable("courseId") Long courseId) {
        boolean flag = courseService.subscribeCourse(currentUser, courseId);
        if (flag)
            return new ResponseEntity<>(new ApiResponse(true, "Вы подписались на курс!"), HttpStatus.OK);
        else return new ResponseEntity<>(new ApiResponse(false, "Ошибка!"), HttpStatus.CONFLICT);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unsubscribeCourse(@CurrentUser UserPrincipal currentUser,
                                                  @PathVariable("courseId") Long courseId) {
        boolean flag = courseService.unsubscribeCourse(currentUser, courseId);
        if (flag)
            return new ResponseEntity<>(new ApiResponse(true, "Вы отписались от курса!"), HttpStatus.OK);
        else return new ResponseEntity<>(new ApiResponse(false, "Ошибка!"), HttpStatus.CONFLICT);
    }

    @PostMapping("{courseId}/lesson")
    public ResponseEntity<?> addLesson(@PathVariable("courseId") Long courseId,
                                       @Valid @RequestBody LessonRequest lessonRequest) {
        Lesson lesson = new Lesson();
        lesson.setTitle(lessonRequest.getTitle());
        lesson.setDescription(lessonRequest.getDescription());
        lesson.setDuration(lessonRequest.getDuration());

        boolean flag = courseService.addLesson(courseId, lesson);

        if (flag)
            return new ResponseEntity<>(HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{courseId}/lessonsdeprecated")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<Lesson>> getCourseLessons(@PathVariable("courseId") Long courseId) {
        List<Lesson> lessons = courseService.getLessonsByCourseId(courseId);
        lessons.sort(Comparator.comparingLong(Lesson::getLessonId));
        return new ResponseEntity<>(lessons, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/lessons")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<List<LessonDto>> getLessons(@PathVariable("courseId") Long courseId,
                                                      @CurrentUser UserPrincipal currentUser) {
        List<LessonDto> lessons = courseService.getAllLessons(courseId, currentUser);
//        lessons.sort(Comparator.comparingLong(LessonInfo::getLessonId));
        return new ResponseEntity<>(lessons, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/lesson/{lessonId}/next")
    public ResponseEntity<?> getNextLesson(@PathVariable("courseId") Long courseId,
                                                @PathVariable("lessonId") Long lessonId) {
        Lesson lesson = courseService.getNextLesson(courseId, lessonId);
        if (lesson != null)
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Следующего урока не существует!"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{courseId}/lesson/{lessonId}/previous")
    public ResponseEntity<?> getPreviousLesson(@PathVariable("courseId") Long courseId,
                                               @PathVariable("lessonId") Long lessonId) {
        Lesson lesson = courseService.getPreviousLesson(courseId, lessonId);
        if (lesson != null)
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Предыдущего урока не существует!"), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("{courseId}/video")
    public UploadFileResponse addVideoToCourse(@PathVariable("courseId") Long courseId,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        Course course = courseRepository.findByCourseId(courseId);
        if (course.getVideo() != null)
            videoService.deleteVideo(course.getVideo());

        Video video = videoService.store(file);
        course.setVideo(video);
        courseRepository.save(course);

        return new UploadFileResponse(video.getUrl(), video.getType(), file.getSize());
    }

    @GetMapping("{courseId}/dto")
    public ResponseEntity<?> getCourseInfo(@CurrentUser UserPrincipal currentUser,
                                           @PathVariable("courseId") Long courseId) {
//        Course course = courseRepository.findByCourseId(courseId);
//        boolean subscribed = false;
//        if (currentUser != null) {
//            User user = userRepository.findByUserId(currentUser.getUserId());
//            subscribed = user.getCourses().contains(course);
//        }
//        if (course == null)
//            return new ResponseEntity<>(new ApiResponse(false, "course doesn't exist"), HttpStatus.BAD_REQUEST);

//        CourseInfo info = course.courseInfo(subscribed);
        CoursePageDto info = courseService.getCourseInfo(currentUser, courseId);
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
