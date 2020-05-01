package com.lac.controller;

import com.lac.model.File;
import com.lac.model.Image;
import com.lac.payload.UploadFileResponse;
import com.lac.repository.FileRepository;
import com.lac.service.ImageService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("api/resources")
@AllArgsConstructor
public class ResourceController {

    private final ImageService imageService;

    private final FileRepository fileRepository;

    @PostMapping("/image/{type}")
    public UploadFileResponse uploadImage(@RequestParam(name = "file") MultipartFile file,
                                          @PathVariable(name = "type") String type) throws IOException {
        Image image = imageService.storeResourceImage(file, type);
        return new UploadFileResponse(image.getUrl(), image.getType(), file.getSize());
    }

    @GetMapping("/image/{type}")
    public ResponseEntity<?> getImage(@PathVariable("type") String type) {
        List<File> files = fileRepository.findAllByUrlContains("/resources/" + type + "/");
//        if (files.size() == 1) {
//            Image image = (Image)files.get(0);
//            return new ResponseEntity<>(image, HttpStatus.OK);
//        }
        Random rnd = new Random();
        Image image = (Image) files.get(rnd.nextInt(files.size()));
        return new ResponseEntity<>(image, HttpStatus.OK);
    }
}
