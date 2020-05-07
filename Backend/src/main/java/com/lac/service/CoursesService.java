package com.lac.service;

import com.amazonaws.services.s3.AmazonS3;
import com.lac.model.*;
import com.lac.payload.CourseInfo;
import com.lac.payload.SortName;
import com.lac.repository.CategoryRepository;
import com.lac.repository.CourseRepository;
import com.lac.repository.UserRepository;
import com.lac.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.apache.catalina.startup.Catalina;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursesService {

    private final CourseRepository courseRepository;

    private final UserRepository userRepository;

    private final CategoryRepository categoryRepository;

    private final AmazonS3 awsS3Client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    private static final String ENDPOINT_URL = "https://lacbucket.s3.eu-west-2.amazonaws.com";

    private static final Logger logger = LoggerFactory.getLogger(CoursesService.class);

    public List<CourseInfo> getAllCourses(UserPrincipal currentUser) {
        List<Course> courseList = courseRepository.findAll();
        return getCourseInfos(currentUser, courseList);
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<CourseInfo> getCoursesByUser(UserPrincipal currentUser) {
        User user = userRepository.findByUserId(currentUser.getUserId());
        List<Course> courseList = user.getCourses();
        List<CourseInfo> courses = new ArrayList<>();
        for (Course course : courseList)
            courses.add(course.courseInfo(true));
        return courses;
    }

    public List<CourseInfo> getCoursesByCategory(Long categoryId, UserPrincipal currentUser) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        List<Course> courseList = courseRepository.findAllByCategory(category);
        return getCourseInfos(currentUser, courseList);
    }

    public List<Course> getCoursesByCategory(Long categoryId) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        return courseRepository.findAllByCategory(category);
    }

    public List<CourseInfo> getCoursesByCategoryAndSorted(Long categoryId, Integer sortId,
                                                      UserPrincipal currentUser) {
        SortName sortName = SortName.values()[sortId];
        Category category = categoryRepository.findByCategoryId(categoryId);
        List<Course> courseList;

        Sort sort = getSort(sortName);

        if (sort == null)
            courseList = courseRepository.findByCategoryAndSortedBySubsNumber(category);
        else courseList = courseRepository.findAllByCategory(category, sort);

        return getCourseInfos(currentUser, courseList);
    }

    public List<CourseInfo> getCoursesByTitleSubstring(String substring, UserPrincipal currentUser) {
        List<Course> courseList = courseRepository.findAllByTitleContaining(substring);
        return getCourseInfos(currentUser, courseList);
    }

    public List<Course> getCoursesByTitleSubstring(String substring) {
        return courseRepository.findAllByTitleContaining(substring);
    }

    public List<CourseInfo> getCoursesByTitleSubstringAndSorted(String substring, Integer sortId,
                                                                UserPrincipal currentUser ) {
        SortName sortName = SortName.values()[sortId];
        List<Course> courseList;

        Sort sort = getSort(sortName);

        if (sort == null)
            courseList = courseRepository.findByTitleContainingAndSortedBySubsNumber(substring);
        else courseList = courseRepository.findAllByTitleContaining(substring, sort);

        return getCourseInfos(currentUser, courseList);
    }

    public List<CourseInfo> getCoursesByCategoryAndTitleAndSorted(Long categoryId, String substring,
                                                                  Integer sortId, UserPrincipal currentUser) {
        Category category = categoryRepository.findByCategoryId(categoryId);
        SortName sortName = SortName.values()[sortId];
        List<Course> courseList;

        Sort sort = getSort(sortName);

        if (sort == null)
            courseList = courseRepository.findByCategoryAndTitleAndSortedBySubsNumber(category, substring);
        else courseList = courseRepository.findAllByCategoryAndTitleContaining(category, substring, sort);

        return getCourseInfos(currentUser, courseList);
    }

    public List<CourseInfo> getTopCourses(UserPrincipal currentUser) {
        List<CourseInfo> courses = new ArrayList<>();
        List<Course> courseList = courseRepository.findTopPopularCourses();
        int counter = 0;
        for (Course course : courseList) {
            boolean subscribed = false;
            if (currentUser != null) {
                User user = userRepository.findByUserId(currentUser.getUserId());
                subscribed = user.getCourses().contains(course);
            }
            if (!subscribed) {
                CourseInfo info = course.courseInfo(false);
                courses.add(info);
                counter++;
                if (counter == 5)
                    break;
            }
        }
        return courses;
    }

    public List<Course> getTopCourses() {
        Pageable pageable = PageRequest.of(0, 5);
        return courseRepository.findTopPopularCourses(pageable);
    }

    public List<CourseInfo> getCoursesSortedByTitleAsc(UserPrincipal currentUser) {
        Sort sort = new Sort(Sort.Direction.ASC, "title");
        List<Course> courseList = courseRepository.findAll(sort);
        return getCourseInfos(currentUser, courseList);
    }

    public List<CourseInfo> getCoursesSortedByTitleDesc(UserPrincipal currentUser) {
        Sort sort = new Sort(Sort.Direction.DESC, "title");
        List<Course> courseList = courseRepository.findAll(sort);
        return getCourseInfos(currentUser, courseList);
    }

    public List<CourseInfo> getCoursesSortedByRateDesc(UserPrincipal currentUser) {
        Sort sort = new Sort(Sort.Direction.DESC, "mark");
        List<Course> courseList = courseRepository.findAll(sort);
        return getCourseInfos(currentUser, courseList);
    }

    public List<CourseInfo> getCoursesSortedBySubscribersNumber(UserPrincipal currentUser) {
        List<Course> courseList = courseRepository.findTopPopularCourses();
        return getCourseInfos(currentUser, courseList);
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

        for (Lesson l : course.getLessons())
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

    private List<CourseInfo> getCourseInfos(UserPrincipal currentUser, List<Course> courseList) {
        List<CourseInfo> courses = new ArrayList<>();
        for (Course course : courseList) {
            boolean subscribed = false;
            if (currentUser != null) {
                User user = userRepository.findByUserId(currentUser.getUserId());
                subscribed = user.getCourses().contains(course);
            }
            CourseInfo info = course.courseInfo(subscribed);
            courses.add(info);
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
}
