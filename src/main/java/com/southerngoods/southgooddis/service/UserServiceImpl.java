package com.southerngoods.southgooddis.service;

import com.southerngoods.southgooddis.dto.UserDto;
import com.southerngoods.southgooddis.model.User;
import com.southerngoods.southgooddis.repository.RoleRepository;
import com.southerngoods.southgooddis.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, RoleRepository roleRepository) { // Update constructor
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User save(UserDto userDto) {
        User user;
        if (userDto.getId() != null) {
            user = userRepository.findById(userDto.getId()).orElse(new User());
        } else {
            user = new User();
        }

        user.setUsername(userDto.getUsername());
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());

        if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
            user.setPassword(userDto.getPassword());
        }

        // Fetch the Role entity from the database using the role ID from the DTO
        if (userDto.getRole() != null && userDto.getRole().getId() != null) {
            roleRepository.findById(userDto.getRole().getId()).ifPresent(user::setRole);
        }

        return userRepository.save(user);
    }

    @Override
    public void disableUser(Long id) {
        User user = findById(id);
        if (user != null) {
            user.setEnabled(false);
            userRepository.save(user);
        }
    }
}