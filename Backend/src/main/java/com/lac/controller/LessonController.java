package com.lac.controller;

import com.lac.model.Comment;
import com.lac.model.Lesson;
import com.lac.model.Video;
import com.lac.payload.CommentRequest;
import com.lac.payload.UploadFileResponse;
import com.lac.repository.LessonRepository;
import com.lac.repository.UserRepository;
import com.lac.security.CurrentUser;
import com.lac.security.UserPrincipal;
import com.lac.service.CommentService;
import com.lac.service.VideoService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("api/lesson")
@AllArgsConstructor
public class LessonController {

    private final CommentService commentService;

    private final UserRepository userRepository;

    private final VideoService videoService;

    private final LessonRepository lessonRepository;

    @PostMapping("{lessonId}/comment")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<Comment> addCommentToLesson(@CurrentUser UserPrincipal currentUser,
                                                      @Valid @RequestBody CommentRequest request,
                                                      @PathVariable("lessonId") Long lessonId) {
        Comment comment = new Comment(request.getText());
//        comment.setDate(new Date());
        comment.setUser(userRepository.findByUserId(currentUser.getUserId()));
        boolean flag = commentService.addCommentToLesson(lessonId, comment);
        if (!flag)
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        return new ResponseEntity<>(comment, HttpStatus.CREATED);
    }

    @PostMapping("{lessonId}/video")
    public UploadFileResponse addVideoToLesson(@PathVariable("lessonId") Long lessonId,
                                               @RequestParam("file") MultipartFile file) throws IOException {
        Lesson lesson = lessonRepository.findByLessonId(lessonId);
        if (lesson.getVideo() != null)
            videoService.deleteVideo(lesson.getVideo());

        Video video = videoService.store(file);
        lesson.setVideo(video);
        lessonRepository.save(lesson);

        return new UploadFileResponse(video.getUrl(), video.getType(), file.getSize());
    }

    @PostMapping("{lessonId}/videourl")
    public ResponseEntity<?> addVideoUrl(@PathVariable("lessonId") Long lessonId,
                                         @RequestParam("url") String url) {
        Lesson lesson = lessonRepository.findByLessonId(lessonId);
        Video video = videoService.saveUrl(url);

        lesson.setVideo(video);
        lessonRepository.save(lesson);

        return new ResponseEntity<>(HttpStatus.OK);
    }
}
