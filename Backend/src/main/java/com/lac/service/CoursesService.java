package com.lac.service;

import com.amazonaws.services.s3.AmazonS3;
import com.lac.dto.CourseDto;
import com.lac.dto.SearchPageCourseDto;
import com.lac.dto.UserPageCourseDto;
import com.lac.dto.mapper.EntityToDtoMapper;
import com.lac.model.*;
import com.lac.payload.SortName;
import com.lac.repository.*;
import com.lac.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.commons.text.similarity.FuzzyScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class CoursesService {

    private final CourseRepository courseRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final CommentRepository commentRepository;

    private final LessonRepository lessonRepository;

    private final ProgressRepository progressRepository;

    private final AmazonS3 awsS3Client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    private static final String ENDPOINT_URL = "https://lacbucket.s3.eu-west-2.amazonaws.com";

    private final EntityToDtoMapper entityToDtoMapper = new EntityToDtoMapper();

    private static final Logger logger = LoggerFactory.getLogger(CoursesService.class);

//    public List<CoursePageDto> getAllCourses(UserPrincipal currentUser) {
//        List<Course> courseList = courseRepository.findAllCourses();
//        return getCourseInfos(currentUser, courseList);
//    }

    public List<CourseDto> getCourses() {
        List<Course> courseList = courseRepository.findAllCourses();
        return getCourseDtos(courseList);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAllCourses();
    }

    public List<UserPageCourseDto> getCoursesByUser(UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        List<Course> courseList = new ArrayList<>(user.getCourses());
        return getUserPageCourseDtos(courseList, currentUser);
    }

    public List<SearchPageCourseDto> getFilteredCourses(Long categoryId, String substring, Integer sortId) {
        List<SearchPageCourseDto> courses;

        if (categoryId == null && substring.isEmpty()) {
            List<Course> coursesList = courseRepository.findAll();
            courses = getSearchPageCourseDtos(coursesList);
        }
        else if (categoryId == null)
            courses = getCoursesByTitleSubstringAndSorted(substring, sortId);
        else if (substring.isEmpty())
            courses = getCoursesByCategoryAndSorted(categoryId, sortId);
        else courses = getCoursesByCategoryAndTitleAndSorted(categoryId, substring, sortId);

        return courses;
    }

    public List<CourseDto> getCoursesDtoByCategory(Long categoryId) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        List<Course> courseList = courseRepository.findAllByCategory(category);
        return getCourseDtos(courseList);
    }

    public List<Course> getCoursesByCategory(Long categoryId) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        return courseRepository.findAllByCategory(category);
    }

    private List<SearchPageCourseDto> getCoursesByCategoryAndSorted(Long categoryId, Integer sortId) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        List<Course> courseList;

        if (sortId != null) {
            SortName sortName = SortName.values()[sortId];

            Sort sort = getSort(sortName);

            if (sort == null)
                courseList = courseRepository.findByCategoryAndSortedBySubsNumber(category);
            else courseList = courseRepository.findAllByCategory(category, sort);
        }
        else courseList = courseRepository.findAllByCategory(category);

        return getSearchPageCourseDtos(courseList);
    }

    public List<SearchPageCourseDto> getCoursesDtoByTitleSubstring(String substring) {
//        List<Course> courseList = courseRepository.findAllByTitleContaining(substring);
        List<Course> courseList = courseRepository.findAll();
        List<Course> courses = fuzzySearch(courseList, substring);
        return getSearchPageCourseDtos(courses);
    }

    public List<Course> getCoursesByTitleSubstring(String substring) {
        return courseRepository.findAllByTitleContaining(substring);
    }

    private List<SearchPageCourseDto> getCoursesByTitleSubstringAndSorted(String substring, Integer sortId) {
        List<Course> courseList;
        courseList = courseRepository.findAll();
        List<Course> courses = fuzzySearch(courseList, substring);
        sortCourses(courses, sortId);
//        if (sortId != null) {
//            SortName sortName = SortName.values()[sortId];
//
//
//            Sort sort = getSort(sortName);
//
//            if (sort == null)
//                courseList = courseRepository.findByTitleContainingAndSortedBySubsNumber(substring);
//            else courseList = courseRepository.findAllByTitleContaining(substring, sort);
//        }
//        else courseList = courseRepository.findAllByTitleContaining(substring);

        return getSearchPageCourseDtos(courses);
    }

    private List<SearchPageCourseDto> getCoursesByCategoryAndTitleAndSorted(Long categoryId, String substring,
                                                                            Integer sortId) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        List<Course> courseList = courseRepository.findAllByCategory(category);
        List<Course> courses = fuzzySearch(courseList, substring);
        sortCourses(courses, sortId);
//        if (sortId != null) {
//            SortName sortName = SortName.values()[sortId];
//            Sort sort = getSort(sortName);
//
//            if (sort == null)
//                courseList = courseRepository.findByCategoryAndTitleAndSortedBySubsNumber(category, substring);
//            else courseList = courseRepository.findAllByCategoryAndTitleContaining(category, substring, sort);
//        }
//        else courseList = courseRepository.findAllByCategoryAndTitleContaining(category, substring);

        return getSearchPageCourseDtos(courses);
    }

    public List<SearchPageCourseDto> getTopCoursesDto() {
        List<SearchPageCourseDto> courses = new ArrayList<>();
        List<Course> courseList = courseRepository.findTopPopularCourses();
        int counter = 0;
        for (Course course : courseList) {
//            boolean subscribed = false;
//            if (currentUser != null) {
//                User user = userRepository.findByUserId(currentUser.getUserId());
//                subscribed = user.getCourses().contains(course);
//            }
//            if (!subscribed) {
//                List<Comment> comments = commentRepository.findAllByCourse(course);
//                List<Lesson> lessons = lessonRepository.findAllByCourse(course);
//                CoursePageDto info = course.courseInfo(false, comments.size(), lessons.size());
                SearchPageCourseDto dto = entityToDtoMapper.courseToSearchPageDto(course);
                courses.add(dto);
                counter++;
                if (counter == 5)
                    break;
//            }
        }
        return courses;
    }

    public List<Course> getTopCourses() {
        Pageable pageable = PageRequest.of(0, 5);
        return courseRepository.findTopPopularCourses(pageable);
    }

    public List<UserPageCourseDto> getCoursesInProgress(UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        Set<Course> courseList = user.getCourses();
        Progress progress = progressRepository.findByUser(user);

        List<Course> courses = new ArrayList<>();
        for (Course course : courseList) {
            List<Lesson> lessons = lessonRepository.findAllByCourse(course);
                if (progress == null || !progress.getLessons().containsAll(lessons) || lessons.size() == 0)
                    courses.add(course);
        }

        return getUserPageCourseDtos(courses, currentUser);
    }

    public List<UserPageCourseDto> getCompletedCourses(UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        Set<Course> courseList = user.getCourses();
        Progress progress = progressRepository.findByUser(user);

        List<Course> courses = new ArrayList<>();
        if (progress != null) {
            for (Course course : courseList) {
                List<Lesson> lessons = lessonRepository.findAllByCourse(course);
                if (lessons.size() > 0) {
                    if (progress.getLessons().containsAll(lessons))
                        courses.add(course);
                }
            }
        }

        return getUserPageCourseDtos(courses, currentUser);
    }

    public boolean addCourse(Course course) {
        if (!courseRepository.existsByTitleAndDescription(course.getTitle(), course.getDescription())) {
            courseRepository.save(course);
            return true;
        }
        return false;
    }

    public boolean removeCourse(Course course) {
        if (course == null)
            return false;

        Image image = course.getImage();
        removeContent(image);
        List<Lesson> lessons = lessonRepository.findAllByCourse(course);
        for (Lesson l : lessons)
            removeContent(l.getVideo());
        courseRepository.delete(course);

        return true;
    }

    private void removeContent(File file) {
        if (!file.getType().equals("empty")) {
            String key = file.getUrl().substring(ENDPOINT_URL.length() + 1);
            awsS3Client.deleteObject(bucketName, key);
        }
    }

//    private List<CoursePageDto> getCourseInfos(UserPrincipal currentUser, List<Course> courseList) {
//        List<CoursePageDto> courses = new ArrayList<>();
//        for (Course course : courseList) {
//            boolean subscribed = false;
//            if (currentUser != null) {
//                User user = userRepository.findByUserId(currentUser.getUserId());
//                subscribed = user.getCourses().contains(course);
//            }
////            List<Comment> reviews = commentRepository.findAllByCourse(course);
////            List<Lesson> lessons = lessonRepository.findAllByCourse(course);
////            long courseId = course.getCourseId();
//            int reviews = commentRepository.countCommentsByCourse(course);
//            int lessons = lessonRepository.countLessonsByCourse(course);
//            CoursePageDto info = course.courseInfo(subscribed, reviews, lessons);
//            courses.add(info);
//        }
//        return courses;
//    }

    private List<CourseDto> getCourseDtos(List<Course> courseList) {
        List<CourseDto> courses = new ArrayList<>();
        for (Course course : courseList) {
//            boolean subscribed = false;
//            if (currentUser != null) {
//                User user = userRepository.findByUserId(currentUser.getUserId());
////                subscribed = user.getCourses().contains(course);
//            }
            CourseDto courseDto = entityToDtoMapper.courseToDto(course);
            courses.add(courseDto);
        }
        return courses;
    }

    private List<SearchPageCourseDto> getSearchPageCourseDtos(List<Course> courseList) {
        List<SearchPageCourseDto> courses = new ArrayList<>();
        for (Course course : courseList) {
            SearchPageCourseDto dto = entityToDtoMapper.courseToSearchPageDto(course);
            courses.add(dto);
        }
        return courses;
    }

    private List<UserPageCourseDto> getUserPageCourseDtos(List<Course> courseList, UserPrincipal currentUser) {
        List<UserPageCourseDto> courses = new ArrayList<>();
        Progress progress = progressRepository.findByUserId(currentUser.getUserId());
        List<Lesson> lessons;

        for (Course course : courseList) {
            lessons = lessonRepository.findAllByCourse(course);
            int lessonsNumber = lessons.size(), lessonsViewed = 0;

            if (progress != null) {
                for (Lesson lesson : lessons) {
                    if (progress.getLessons().contains(lesson))
                        lessonsViewed++;
                }
            }
            boolean completed = lessonsViewed > 0 && lessonsNumber == lessonsViewed;

            UserPageCourseDto dto = entityToDtoMapper.courseToUserPageDto(course, lessonsNumber,
                    lessonsViewed, completed);
            courses.add(dto);
        }

        return courses;
    }

    private Sort getSort(SortName sortName) {
        Sort sort = null;
        switch (sortName) {
            case RATE:
                sort = new Sort(Sort.Direction.DESC, "mark");
                break;
            case TITLE_ASC:
                sort = new Sort(Sort.Direction.ASC, "title");
                break;
            case TITLE_DESC:
                sort = new Sort(Sort.Direction.DESC, "title");
                break;
            case SUBS_NUMBER:
                break;
        }
        return sort;
    }

    private List<Course> fuzzySearch(List<Course> courseList, String substring) {
        FuzzyScore score = new FuzzyScore(Locale.ENGLISH);
        List<Course> courses = new ArrayList<>();
        Map<Integer, Integer> scores = new HashMap<>();

        for (Course c : courseList) {
            int similarity = score.fuzzyScore(c.getTitle(), substring);
            if (similarity > 0) {
                courses.add(c);
                scores.put(Objects.hash(c), similarity);
            }
        }

        courses.sort(Comparator.comparing(c -> scores.get(Objects.hash(c))).reversed());
        return courses;
    }

    private void sortCourses(List<Course> courses, Integer sortId) {
        if (sortId != null) {
            SortName sortName = SortName.values()[sortId];

            if (sortName == SortName.RATE) {
                courses.sort(Comparator.comparing(Course::getMark).reversed());
            }
            else if (sortName == SortName.SUBS_NUMBER) {
                courses.sort(Comparator.comparing(c -> c.getUsers().size()));
                Collections.reverse(courses);
            }
            else {
                courses.sort(Comparator.comparing(Course::getTitle));
                if (sortName == SortName.TITLE_DESC)
                    Collections.reverse(courses);
            }
        }
    }
}
