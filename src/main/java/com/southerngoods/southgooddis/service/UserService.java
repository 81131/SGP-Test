package com.southerngoods.southgooddis.service;

import com.southerngoods.southgooddis.dto.UserDto;
import com.southerngoods.southgooddis.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    User save(UserDto userDto);
    User findById(Long id);
    void disableUser(Long id);
    void enableUser(Long id);
}