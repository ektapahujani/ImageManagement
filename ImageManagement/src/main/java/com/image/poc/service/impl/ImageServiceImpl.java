package com.image.poc.service.impl;

import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.image.poc.entity.Image;
import com.image.poc.entity.User;
import com.image.poc.exception.FileNotFoundException;
import com.image.poc.model.FileMetadata;
import com.image.poc.repository.ImageRepository;
import com.image.poc.repository.UserRepository;
import com.image.poc.service.ImageService;
import com.image.poc.util.Utility;

@EnableConfigurationProperties
@Service
public class ImageServiceImpl implements ImageService {

    @Value("${imgur.clientId}")
    private String clientId;

    @Value("${imgur.imgurBaseUrl}")
    private String imgurBaseUrl;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ImageRepository imageMetadataRepository;

    @Autowired
    private RestTemplate restTemplate;


    private Logger logger = LoggerFactory.getLogger(ImageServiceImpl.class);

    @Override
    public FileMetadata uploadFile(File file, String userName) {

        logger.info("******************* " + imgurBaseUrl);
        logger.info("******************* " + clientId);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Client-ID " + clientId);

        MultiValueMap<String, Object> parts = new LinkedMultiValueMap<>();
        parts.add("image", new FileSystemResource(file));

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(parts, headers);
        ResponseEntity<FileMetadata> response = restTemplate.exchange(imgurBaseUrl,
                HttpMethod.POST, requestEntity, FileMetadata.class);

        saveImageMetadataInUserProfile(response.getBody(), userName);
        CompletableFuture.runAsync(() -> {
            try {
                FileUtils.forceDelete(file);
                String message = userName + " has uploaded image " + file.getName();
            } catch (IOException e) {
                logger.error("Failed to delete the temp file.");
            } catch(Exception e) {
                logger.error(e.getMessage(), e);
            }
        });


        logger.info(response.getBody().toString());

        return response.getBody();
    }

    @Override
    public void deleteFile(String fileOwner, String fileId) throws FileNotFoundException {
        Optional<Image> imageMetadata = imageMetadataRepository.findUserByUserNameAndFileId(fileOwner, fileId);

        logger.info("******************* " + imgurBaseUrl);
        logger.info("******************* " + clientId);

        if(imageMetadata.isEmpty()) {
            throw new FileNotFoundException("File not found");
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Client-ID " + clientId);
        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(headers);
        ResponseEntity<?> response = restTemplate.exchange(imgurBaseUrl+"/"+imageMetadata.get().getDeleteHash(),
                HttpMethod.DELETE, requestEntity, Object.class);


        if(response.getStatusCode().is2xxSuccessful()) {
            imageMetadataRepository.delete(imageMetadata.get());
        }

    }

    @Override
    public String viewFile(String fileOwner, String fileId) throws FileNotFoundException {

        Optional<Image> imageMetadata = imageMetadataRepository.findUserByUserNameAndFileId(fileOwner, fileId);
        if(imageMetadata.isPresent()) {
            String url = imageMetadata.get().getPreviewLink();
            Utility.viewInBrowser(url);
            return url;
        }
        throw new FileNotFoundException("File not found with given details.");
    }

    private void saveImageMetadataInUserProfile(FileMetadata fileMetadata, String userName) {
        Optional<User> user = userRepository.findUserByUserName(userName);

        Image im = new Image();
        im.setFileId(fileMetadata.getData().getId());
        im.setDeleteHash(fileMetadata.getData().getDeletehash());
        im.setPreviewLink(fileMetadata.getData().getLink());
        im.setFileType(fileMetadata.getData().getType());
        im.setUsername(userName);

        user.get().addImageMetadata(im);


        userRepository.save(user.get());

    }
}
