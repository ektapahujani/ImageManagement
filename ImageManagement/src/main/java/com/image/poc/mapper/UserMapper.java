package com.image.poc.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import com.image.poc.entity.Image;
import com.image.poc.entity.User;
import com.image.poc.model.Data;

@Mapper(componentModel = "spring", uses = ImageMapper.class)
public interface UserMapper {

     // UserMapper MAPPER = Mappers.getMapper(UserMapper.class);

    @Mapping(target = "password", constant = "****")
    com.image.poc.model.User mapToUserModel(User user);

    User mapToUserEntity(com.image.poc.model.User user);

}
