package com.lac.service;

import com.lac.model.Lesson;
import com.lac.model.Progress;
import com.lac.model.User;
import com.lac.repository.LessonRepository;
import com.lac.repository.UserRepository;
import com.lac.security.UserPrincipal;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class LessonService {

    private final LessonRepository lessonRepository;

    private final UserRepository userRepository;

    public boolean viewLesson(UserPrincipal currentUser, Long lessonId) {
        Lesson lesson = lessonRepository.findByLessonId(lessonId);
        User user = userRepository.findByUserId(currentUser.getUserId());

        Progress userProgress = user.getProgress();
        if (userProgress == null)
            userProgress = new Progress();

        boolean flag = userProgress.addLesson(lesson);
        if (flag) {
            user.setProgress(userProgress);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
