package com.image.poc.service;

import java.util.Optional;

import com.image.poc.model.User;
import com.image.poc.exception.UserNotFoundException;

public interface UserService {

    Optional<User> registerUser(User user) throws Exception;

    boolean authenticateUser(String userName, String password) throws Exception;

    Optional<User> retrieveUser(String userName) throws UserNotFoundException;
}
