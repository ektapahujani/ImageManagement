package com.image.poc.mapper;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.image.poc.entity.Image;
import com.image.poc.model.Data;

@Mapper(componentModel = "spring")
public interface ImageMapper {

    @Mapping(target = "id", source = "fileId")
    @Mapping(target = "type", source = "fileType")
    @Mapping(target = "deletehash", source = "deleteHash")
    @Mapping(target = "link", source = "previewLink")
    Data toImageData(Image imageMetadata);
}
