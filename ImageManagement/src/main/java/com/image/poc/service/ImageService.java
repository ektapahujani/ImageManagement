package com.image.poc.service;

import java.io.File;

import com.image.poc.exception.FileNotFoundException;
import com.image.poc.model.FileMetadata;

public interface ImageService {


    FileMetadata uploadFile(File file, String userName);
    void deleteFile(String fileOwner, String fileId) throws FileNotFoundException;
    String viewFile(String fileOwner, String fileId) throws FileNotFoundException;
}
