package com.southerngoods.southgooddis.controller;

import com.southerngoods.southgooddis.dto.UserDto;
import com.southerngoods.southgooddis.model.Role;
import com.southerngoods.southgooddis.model.User;
import com.southerngoods.southgooddis.repository.RoleRepository; // <-- Import RoleRepository
import com.southerngoods.southgooddis.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository; // <-- Inject RoleRepository

    public UserController(UserService userService, RoleRepository roleRepository) { // <-- Update constructor
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        model.addAttribute("users", users);
        return "users";
    }

    @GetMapping("/add")
    public String showAddUserForm(Model model) {
        List<Role> roles = roleRepository.findAll(); // <-- Fetch all roles
        model.addAttribute("user", new UserDto());
        model.addAttribute("roles", roles); // <-- Add roles to the model
        return "user-form";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") UserDto userDto) {
        userService.save(userDto);
        return "redirect:/users";
    }
}