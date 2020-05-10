package com.lac.controller;

import com.lac.model.Category;
import com.lac.model.Course;
import com.lac.model.User;
import com.lac.payload.ApiResponse;
import com.lac.payload.CourseInfo;
import com.lac.payload.CourseRequest;
import com.lac.payload.SortName;
import com.lac.repository.CategoryRepository;
import com.lac.repository.CourseRepository;
import com.lac.repository.UserRepository;
import com.lac.security.CurrentUser;
import com.lac.security.UserPrincipal;
import com.lac.service.CoursesService;
import com.lac.service.ImageService;
import com.lac.service.VideoService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CoursesController {

    private final UserRepository userRepository;

    private final CoursesService coursesService;

    private final ImageService imageService;

    private final VideoService videoService;

    private final CategoryRepository categoryRepository;

    private final CourseRepository courseRepository;

    private static final Logger logger = LoggerFactory.getLogger(CoursesController.class);

    @GetMapping
    public ResponseEntity<List<Course>> getAllCourses() {
        List<Course> courses = coursesService.getAllCourses();
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/info")
    public ResponseEntity<List<CourseInfo>> getAllCourses(@CurrentUser UserPrincipal currentUser) {
        List<CourseInfo> courses = coursesService.getAllCourses(currentUser);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

//    @PostMapping("/{courseId}/videos")
//    public List<UploadFileResponse> setCourseVideos(@RequestParam(name = "files") MultipartFile[] files,
//                                                    @PathVariable("courseId") Long courseId) throws IOException {
//        Course course = courseRepository.findByCourseId(courseId);
//
//        Set<Video> videos = new HashSet<>();
//        List<UploadFileResponse> responses = new ArrayList<>();
//
//        for (MultipartFile file : files) {
//            Video video = videoService.store(file);
//            videos.add(video);
//            responses.add(new UploadFileResponse(video.getName(), video.getType(), file.getSize()));
//        }
//
//        course.setVideos(videos);
//        courseRepository.save(course);
//
//        return responses;
//    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getCoursesByUserId(@PathVariable("userId") Long userId) {
        User user = userRepository.findByUserId(userId);
        if (user != null) {
            List<Course> courses = user.getCourses();
            return new ResponseEntity<>(courses, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyCourses(@CurrentUser UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        if (user != null) {
            List<Course> courses = user.getCourses();
            return new ResponseEntity<>(courses, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/me/info")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<?> getMyCoursesInfo(@CurrentUser UserPrincipal currentUser) {
        if (currentUser != null) {
            List<CourseInfo> courses = coursesService.getCoursesByUser(currentUser);
            return new ResponseEntity<>(courses, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @PostMapping
    public ResponseEntity<Void> addCourse(@Valid @RequestBody CourseRequest request) {
        Category category = categoryRepository.findByName(request.getCategoryName());
        Course course = new Course(request.getTitle(), request.getDescription(), request.getDescriptionLong(),
                request.getLanguage(), request.getLoad(), category);
        boolean flag = coursesService.addCourse(course);
        if (!flag)
            return new ResponseEntity<>(HttpStatus.CONFLICT);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ServletUriComponentsBuilder
                .fromCurrentContextPath().path("/api/courses/{title}")
                .buildAndExpand(course.getCourseId()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @DeleteMapping("/{courseId}")
    public ResponseEntity<?> removeCourse(@PathVariable("courseId") Long courseId) {
        Course course = courseRepository.findByCourseId(courseId);
        boolean success = coursesService.removeCourse(course);
        if (success)
            return new ResponseEntity<>(new ApiResponse(true, "Курс удален"), HttpStatus.OK);
        return new ResponseEntity<>(new ApiResponse(false, "Не существует курса с таким ID"), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("/category/{categoryId}/info")
    public ResponseEntity<?> getCoursesByCategoryId(@PathVariable("categoryId") Long categoryId,
                                                    @CurrentUser UserPrincipal currentUser) {
        List<CourseInfo> courses = coursesService.getCoursesByCategory(categoryId, currentUser);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getCoursesByCategoryId(@PathVariable("categoryId") Long categoryId) {
        List<Course> courses = coursesService.getCoursesByCategory(categoryId);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/search/{substring}/info")
    public ResponseEntity<?> getCoursesBySubstring(@PathVariable("substring") String substring,
                                                   @CurrentUser UserPrincipal currentUser) {
        List<CourseInfo> courses = coursesService.getCoursesByTitleSubstring(substring, currentUser);
        if (courses.isEmpty()) {
            courses = coursesService.getTopCourses(currentUser);
        }
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/search/{substring}")
    public ResponseEntity<?> getCoursesBySubstring(@PathVariable("substring") String substring) {
        List<Course> courses = coursesService.getCoursesByTitleSubstring(substring);
        if (courses.isEmpty()) {
            courses = coursesService.getTopCourses();
        }
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/search/{substring}/sort/{sortId}")
    public ResponseEntity<?> getCoursesBySubstringAndSorted(@PathVariable("substring") String substring,
                                                            @PathVariable("sortId") Integer sortId,
                                                            @CurrentUser UserPrincipal currentUser) {
        List<CourseInfo> courses = coursesService
                .getCoursesByTitleSubstringAndSorted(substring, sortId, currentUser);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}/sort/{sortId}")
    public ResponseEntity<?> getCoursesByCategoryAndSorted(@PathVariable("categoryId") Long categoryId,
                                                           @PathVariable("sortId") Integer sortId,
                                                           @CurrentUser UserPrincipal currentUser) {
        List<CourseInfo> courses = coursesService
                .getCoursesByCategoryAndSorted(categoryId, sortId, currentUser);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/category/{categoryId}/search/{substring}/sort/{sortId}")
    public ResponseEntity<?> getCoursesByCategoryAndSubstringAndSorted
            (@PathVariable("categoryId") Long categoryId, @PathVariable("substring") String substring,
             @PathVariable("sortId") Integer sortId, @CurrentUser UserPrincipal currentUser) {
        List<CourseInfo> courses = coursesService.
                getCoursesByCategoryAndTitleAndSorted(categoryId, substring, sortId, currentUser);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }

    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredCourses(@RequestParam(value = "category") Long categoryId,
                                                @RequestParam(value = "substring") String substring,
                                                @RequestParam(value = "sort") Integer sortId,
                                                @CurrentUser UserPrincipal currentUser) {
        List<CourseInfo> courses = coursesService.getFilteredCourses(categoryId, substring, sortId, currentUser);
        return new ResponseEntity<>(courses, HttpStatus.OK);
    }
}
