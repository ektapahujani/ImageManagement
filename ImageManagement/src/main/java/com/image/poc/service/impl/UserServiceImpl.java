package com.image.poc.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.mapstruct.factory.Mappers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import com.image.poc.exception.UserNotFoundException;
import com.image.poc.mapper.ImageMapper;
import com.image.poc.mapper.UserMapper;
import com.image.poc.model.Data;
import com.image.poc.model.User;
import com.image.poc.repository.UserRepository;
import com.image.poc.service.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    private UserMapper userMapper = Mappers.getMapper(UserMapper.class);

    private ImageMapper imageDataMapper = Mappers.getMapper(ImageMapper.class);

    private Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Override
    public Optional<com.image.poc.model.User> registerUser(User user) throws Exception {

        Optional<com.image.poc.entity.User> existingUser = userRepository.findUserByUserName(user.getUsername());
        if(existingUser.isPresent()) {
            logger.error("User is already registered: {}", user.getUsername());
            throw new Exception("User is already registered: "+ user.getUsername());
        }
        com.image.poc.entity.User userEntity = userMapper.mapToUserEntity(user);
        com.image.poc.entity.User dbUser = userRepository.save(userEntity);
        if(null == dbUser) {
            logger.error("Error occurred in Registering the user: {}", user.getUsername());
            throw new Exception("Error occurred in Registering the user: "+ user.getUsername());
        }
        User userModel = userMapper.mapToUserModel(dbUser);
        logger.info("User {} registered successfully.", dbUser.getUsername());
        return Optional.of(userModel);
    }

    @Override
    @Transactional
    public boolean authenticateUser(String userName, String password) throws Exception {

        Optional<com.image.poc.entity.User> dbUser = userRepository.findUserByUserNameAndPassword(userName, password);
        if(!dbUser.isPresent()) {
            throw new UserNotFoundException("User is not registered");
        }
        return userRepository.authenticateUser(userName) == 1;

    }

    @Override
    public Optional<User> retrieveUser(String userName) throws UserNotFoundException {

        Optional<com.image.poc.entity.User> dbUser = userRepository.findUserByUserName(userName);

        if(dbUser.isEmpty()) {
            throw new UserNotFoundException("User is not registered");
        }

        User userModel = userMapper.mapToUserModel(dbUser.get());
        if(!CollectionUtils.isEmpty(dbUser.get().getImageMetadata())) {
            List<Data> data = new ArrayList<>();
            dbUser.get().getImageMetadata().forEach(im -> {
                data.add(imageDataMapper.toImageData(im));
            });
            userModel.setImageData(data);
        }


        return Optional.of(userModel);
    }

	
}
