package com.lac.service;

import com.amazonaws.services.s3.AmazonS3;
import com.lac.model.*;
import com.lac.repository.CourseRepository;
import com.lac.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CoursesService {

    private final CourseRepository courseRepository;

    private final UserRepository userRepository;

    private final AmazonS3 awsS3Client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    private static final String ENDPOINT_URL = "https://lacbucket.s3.eu-west-2.amazonaws.com";

    private static final Logger logger = LoggerFactory.getLogger(CoursesService.class);

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    public List<Course> getCoursesByUser(User user) {
        return user.getCourses();
    }

    public List<Course> getCoursesByCategory(Category category) {
        return courseRepository.findAllByCategory(category);
    }

    public List<Course> getCoursesByTitleSubstring(String substring) {
        return courseRepository.findAllByTitleContaining(substring);
    }

    public List<Course> getTopCourses(Pageable pageable) {
        return courseRepository.findTopPopularCourses(pageable);
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
}
