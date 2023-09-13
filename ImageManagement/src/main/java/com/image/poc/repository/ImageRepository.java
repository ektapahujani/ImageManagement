package com.image.poc.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.image.poc.entity.Image;
import com.image.poc.entity.User;
import com.image.poc.model.FileMetadata;

import java.util.Optional;

public interface ImageRepository extends JpaRepository<Image, Long> {

    @Query("SELECT im FROM Image im WHERE im.username = :username and im.fileId = :fileId")
    Optional<Image> findUserByUserNameAndFileId(@Param("username") String userName,
                                                       @Param("fileId") String fileId);
}
