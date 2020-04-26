package com.lac.controller;


import com.lac.model.*;
import com.lac.payload.*;
import com.lac.repository.CommentRepository;
import com.lac.repository.CourseRepository;
import com.lac.repository.LessonRepository;
import com.lac.repository.UserRepository;
import com.lac.security.CurrentUser;
import com.lac.security.UserPrincipal;
import com.lac.service.CommentService;
import com.lac.service.CourseService;
import com.lac.service.ImageService;
import com.lac.service.VideoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import lombok.Builder;

@RestController
@RequestMapping("api/course/")
public class CourseController {

    @Autowired
    CommentService commentService;

    @Autowired
    private CourseService courseService;

    @Autowired
    private VideoService videoService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageService imageService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private LessonRepository lessonRepository;

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
    public ResponseEntity<Set<Comment>> getAllCourseComments(@PathVariable("courseId") Long courseId) {
        Set<Comment> comments = courseService.getAllCommentsByCourseId(courseId);
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    @PostMapping("{courseId}/comment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> addCommentToCourse(@CurrentUser UserPrincipal currentUser,
                                                @Valid @RequestBody CommentRequest request,
                                                @PathVariable("courseId") Long courseId) {
        Comment comment = new Comment(request.getText());
        comment.setUser(userRepository.findByUserId(currentUser.getUserId()));
        boolean flag = commentService.addCommentToCourse(courseId, comment);
        if (!flag)
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        return new ResponseEntity<>(HttpStatus.CREATED);
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
            return new ResponseEntity<>(new ApiResponse(true, "You subscribed to the course"), HttpStatus.OK);
        else return new ResponseEntity<>(new ApiResponse(false, "Error"), HttpStatus.CONFLICT);
    }

    @DeleteMapping("/{courseId}")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> unsubscribeCourse(@CurrentUser UserPrincipal currentUser,
                                               @PathVariable("courseId") Long courseId) {
        boolean flag = courseService.unsubscribeCourse(currentUser, courseId);
        if (flag)
            return new ResponseEntity<>(new ApiResponse(true, "You unsubscribed from the course"), HttpStatus.OK);
        else return new ResponseEntity<>(new ApiResponse(false, "Error"), HttpStatus.CONFLICT);
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

//    @PostMapping(value = "{courseId}/lessonvideo", consumes = {"multipart/mixed"})
//    public ResponseEntity<?> addLessonWithVideo(@PathVariable("courseId") Long courseId,
//                                                @Valid @RequestPart LessonRequest lessonRequest,
//                                                @RequestParam("file") MultipartFile file) throws IOException {
//        Lesson lesson = new Lesson();
//        lesson.setTitle(lessonRequest.getTitle());
//        lesson.setDescription(lessonRequest.getDescription());
//        lesson.setDuration(lessonRequest.getDuration());
//
//        Video video = videoService.store(file);
//        lesson.setVideo(video);
//        lessonRepository.save(lesson);
//
//        Course course = courseRepository.findByCourseId(courseId);
//        course.addLesson(lesson);
//        courseRepository.save(course);
//
//        return new ResponseEntity<>(new UploadFileResponse(video.getUrl(), video.getType(), file.getSize()), HttpStatus.OK);
//    }

    @GetMapping("/{courseId}/lessons")
    public ResponseEntity<List<Lesson>> getCourseLessons(@PathVariable("courseId") Long courseId) {
        List<Lesson> lessons = courseService.getLessonsByCourseId(courseId);
        return new ResponseEntity<>(lessons, HttpStatus.OK);
    }

    @GetMapping("/{courseId}/lesson/{lessonId}/next")
    public ResponseEntity<?> getNextLesson(@PathVariable("courseId") Long courseId,
                                                @PathVariable("lessonId") Long lessonId) {
        Lesson lesson = courseService.getNextLesson(courseId, lessonId);
        if (lesson != null)
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Next lesson doesn't exist"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/{courseId}/lesson/{lessonId}/previous")
    public ResponseEntity<?> getPreviousLesson(@PathVariable("courseId") Long courseId,
                                               @PathVariable("lessonId") Long lessonId) {
        Lesson lesson = courseService.getPreviousLesson(courseId, lessonId);
        if (lesson != null)
            return new ResponseEntity<>(lesson, HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Previous lesson doesn't exist"), HttpStatus.BAD_REQUEST);
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

    @GetMapping("{courseId}/info")
    public ResponseEntity<?> getCourseInfo(@CurrentUser UserPrincipal currentUser,
                                           @PathVariable("courseId") Long courseId) {
        Course course = courseRepository.findByCourseId(courseId);
        boolean subscribed = false;
        if (currentUser != null) {
            User user = userRepository.findByUserId(currentUser.getUserId());
            subscribed = user.getCourses().contains(course);
        }
        if (course == null)
            return new ResponseEntity<>(new ApiResponse(false, "course doesn't exist"), HttpStatus.BAD_REQUEST);
        CourseInfo info = CourseInfo.builder()
                .courseId(course.getCourseId())
                .category(course.getCategory().getName())
                .description(course.getDescription())
                .descriptionLong(course.getDescriptionLong())
                .imageUrl(course.getImage().getUrl())
                .lessonsNumber(course.getLessons().size())
                .mark(course.getMark())
                .marksNumber(course.getNumMarks())
                .reviewsNumber(course.getComments().size())
                .subsNumber(course.getUsers().size())
                .title(course.getTitle())
                .subscribed(subscribed)
                .build();
        return new ResponseEntity<>(info, HttpStatus.OK);
    }
}
