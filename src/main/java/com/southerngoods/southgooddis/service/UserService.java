package com.southerngoods.southgooddis.service;

import com.southerngoods.southgooddis.dto.UserDto;
import com.southerngoods.southgooddis.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Optional<User> findById(Long id);
    User save(UserDto userDto);
}