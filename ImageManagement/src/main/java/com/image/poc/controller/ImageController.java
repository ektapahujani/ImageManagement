package com.image.poc.controller;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.image.poc.exception.FileNotFoundException;
import com.image.poc.service.ImageService;
import com.image.poc.util.Utility;

@RestController
public class ImageController {

    @Autowired
    private ImageService imageService;

    private Logger logger = LoggerFactory.getLogger(ImageController.class);

    @PostMapping("/image/upload")
    public ResponseEntity<?> uploadImage(@RequestPart("file") MultipartFile inputFile,
                                                    @RequestParam("userName") String userName) {

        if(null == inputFile) {
            return new ResponseEntity<>("File is missing", HttpStatus.BAD_REQUEST);
        }
        try {
            logger.info("Image upload started, fileName: {}, user: {}", inputFile.getName(), userName);
            File convertedFile = Utility.convertToFile(inputFile);
            imageService.uploadFile(convertedFile, userName);
        } catch(Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity("Failed to upload file, please check file or try again later", HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
    }

    @DeleteMapping("/image/delete/{fileId}")
    public ResponseEntity<?> deleteImage(@RequestParam("userName") String userName,
                                         @PathVariable("fileId") String fileId) {

        logger.info("Image delete process started, user {}, file {}", userName, fileId);
        if(fileId.isEmpty()) {
            return new ResponseEntity<>("File Id is missing", HttpStatus.BAD_REQUEST);
        }
        try {
            imageService.deleteFile(userName, fileId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return new ResponseEntity<>("File deleted successfully", HttpStatus.OK);
    }

    @GetMapping("/image/delete/{fileId}")
    public ResponseEntity<Object> viewImage(@RequestParam("userName") String userName,
                                         @PathVariable("fileId") String fileId) {

        logger.info("Image view process started, user {}, file {}", userName, fileId);
        if(fileId.isEmpty()) {
            return new ResponseEntity<>("File Id is missing", HttpStatus.BAD_REQUEST);
        }
        Map<String, String> response = null;
        try {
            String url = imageService.viewFile(userName, fileId);
            response = new HashMap<>();
            response.put("previewLink", url);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return new ResponseEntity(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
