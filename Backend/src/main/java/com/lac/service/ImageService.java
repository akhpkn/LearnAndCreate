package com.lac.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.lac.model.Image;
import com.lac.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.imgscalr.Scalr;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final FileRepository fileRepository;

    private final AmazonS3 awsS3Client;

    @Value("${jsa.s3.bucket}")
    private String bucketName;

    private static final String ENDPOINT_URL = "https://lacbucket.s3.eu-west-2.amazonaws.com";

    public Image store(MultipartFile multipartFile) throws IOException {
        String extension = multipartFile.getOriginalFilename()
                .substring(multipartFile.getOriginalFilename().lastIndexOf('.')).toLowerCase();

        if (ImageIO.read(multipartFile.getInputStream()) == null)
            throw new IOException("Can't read file");

        String fileName = generateFileName(multipartFile.getOriginalFilename());

        String url = resizeAndUpload(fileName + extension, multipartFile);
        Image image = new Image(url, multipartFile.getContentType());
        return fileRepository.save(image);
    }

    private File convertMultiPartToFile(MultipartFile file) throws IOException {
        File convertedFile = new File(file.getOriginalFilename());
        FileOutputStream fos = new FileOutputStream(convertedFile);
        fos.write(file.getBytes());
        fos.close();
        return convertedFile;
    }

    private String upload(String fileName, MultipartFile multipartFile) throws IOException {
//        File file = convertMultiPartToFile(multipartFile);
        String fileUrl = ENDPOINT_URL + "/images/" + fileName;
//        awsS3Client.putObject(new PutObjectRequest(bucketName, fileName, file)
//                .withCannedAcl(CannedAccessControlList.PublicRead));
//        file.delete();
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(multipartFile.getContentType());
        metadata.setContentLength(multipartFile.getSize());
//        awsS3Client.putObject(bucketName, "images/" + fileName, multipartFile.getInputStream(), metadata);
        awsS3Client.putObject(new PutObjectRequest(bucketName, "images/" + fileName,
                multipartFile.getInputStream(), metadata)
                .withCannedAcl(CannedAccessControlList.PublicRead));

        BufferedImage og = ImageIO.read(multipartFile.getInputStream());
        BufferedImage scaledImage = Scalr.resize(og, 50);
        return fileUrl;
    }

    private String resizeAndUpload(String fileName, MultipartFile multipartFile) throws IOException {
        String fileUrl = ENDPOINT_URL + "/images/" + fileName;

        BufferedImage og = ImageIO.read(multipartFile.getInputStream());
        BufferedImage scaledImage = Scalr.resize(og, 400, 400);
        String formatName = multipartFile.getOriginalFilename()
                .substring(multipartFile.getOriginalFilename().lastIndexOf('.') + 1).toLowerCase();

        File output = new File(multipartFile.getOriginalFilename());
        OutputStream stream = new FileOutputStream(multipartFile.getOriginalFilename());
        ImageIO.write(scaledImage, formatName, stream);
        stream.close();

        awsS3Client.putObject(new PutObjectRequest(bucketName, "images/" + fileName, output)
                .withCannedAcl(CannedAccessControlList.PublicRead));
        output.delete();

        return fileUrl;
    }

    public void deleteImage(Image image) {
        String key = image.getUrl().substring(ENDPOINT_URL.length() + 1);
        fileRepository.delete(image);
        awsS3Client.deleteObject(bucketName, key);
    }

    private String generateFileName(String originalName) {
        return DigestUtils.md5Hex(originalName + LocalDateTime.now());
    }

    public Image getFile(Long fileId) {
        return (Image) fileRepository.findByFileId(fileId);
    }
}
