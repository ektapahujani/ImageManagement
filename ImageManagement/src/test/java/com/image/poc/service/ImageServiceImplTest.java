package com.image.poc.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.Optional;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.image.poc.entity.Image;
import com.image.poc.exception.FileNotFoundException;
import com.image.poc.model.Data;
import com.image.poc.model.FileMetadata;
import com.image.poc.repository.ImageRepository;
import com.image.poc.repository.UserRepository;
import com.image.poc.service.impl.ImageServiceImpl;

@RunWith(SpringRunner.class)
@SpringBootTest()
public class ImageServiceImplTest {

    @Value("${imgur.clientId}")
    private String clientId;

    @Value("${imgur.imgurBaseUrl}")
    private String imgurBaseUrl;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageRepository imageMetadataRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ImageServiceImpl imageService;

    @Before
    public void init() {
        ReflectionTestUtils.setField(imageService, "clientId", "abc123");
        ReflectionTestUtils.setField(imageService, "imgurBaseUrl", "www.google.com");
    }

    @Test
    public void testUploadFile_Success() {

        FileMetadata fm = new FileMetadata();
        fm.setStatus(200);
        fm.setSuccess(true);
        Data data = new Data();
        data.setDeletehash("abc123");
        data.setId("111");
        data.setLink("www.google.com");
        data.setType("image/jpg");
        fm.setData(data);

        com.image.poc.entity.User dbUser = new com.image.poc.entity.User();
        dbUser.setAge(50);
        dbUser.setEmail("abc@email.com");

        ResponseEntity<FileMetadata> response = new ResponseEntity<>(fm, HttpStatus.OK);
        File file = new File("./src/test/resources/sample_image.jpg");
        when(userRepository.save(any())).thenReturn(dbUser);
        when(userRepository.findUserByUserName(Mockito.anyString())).thenReturn(Optional.of(dbUser));
        when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.<Class<FileMetadata>>any())
        ).thenReturn(response);
        FileMetadata fileMetadata = imageService.uploadFile(file, "ekta");
        assertNotNull(fileMetadata);
        assertTrue(fileMetadata.isSuccess());
        assertNotNull(fileMetadata.getData());
        assertEquals(200, fileMetadata.getStatus());
        assertEquals("abc123", fileMetadata.getData().getDeletehash());
    }

    @Test(expected = RestClientException.class)
    public void testUploadFile_Failure() {
        File file = new File("./");
        when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.POST), Mockito.any(HttpEntity.class), Mockito.<Class<FileMetadata>>any())
        ).thenThrow(RestClientException.class);

        imageService.uploadFile(file, "ekta");

    }

    @Test(expected = FileNotFoundException.class)
    public void testDeleteFile_Failure() throws FileNotFoundException {

        when(imageMetadataRepository.findUserByUserNameAndFileId(Mockito.anyString(), Mockito.anyString())).thenReturn(Optional.empty());
        imageService.deleteFile("ekta", "abc123");
    }

    @Test
    public void testDeleteFile_Success() throws FileNotFoundException {
        ResponseEntity<FileMetadata> response = new ResponseEntity<>(HttpStatus.OK);
        when(imageMetadataRepository.findUserByUserNameAndFileId(Mockito.anyString(), Mockito.anyString()))
                .thenReturn(Optional.of(createImageMetadata()));
        doNothing().when(imageMetadataRepository).delete(Mockito.any());
        when(restTemplate.exchange(Mockito.anyString(), Mockito.eq(HttpMethod.DELETE), Mockito.any(HttpEntity.class), Mockito.<Class<FileMetadata>>any())
        ).thenReturn(response);
        imageService.deleteFile("ekta", "abc123");

        verify(imageMetadataRepository, times(1)).delete(Mockito.any());
    }

    private Image createImageMetadata() {

        Image imageMetadata = new Image();
        imageMetadata.setUsername("ekta");
        imageMetadata.setDeleteHash("sxw3w");

        return imageMetadata;
    }


}
