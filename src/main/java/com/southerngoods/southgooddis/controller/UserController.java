package com.southerngoods.southgooddis.controller;

import com.southerngoods.southgooddis.dto.UserDto;
import com.southerngoods.southgooddis.model.Role;
import com.southerngoods.southgooddis.model.User;
import com.southerngoods.southgooddis.repository.RoleRepository;
import com.southerngoods.southgooddis.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final RoleRepository roleRepository;

    public UserController(UserService userService, RoleRepository roleRepository) {
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userService.findAll();
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("roles", roles);
        model.addAttribute("user", new UserDto()); // Add empty DTO for the modal form
        return "users";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute("user") UserDto userDto) {
        userService.save(userDto);
        return "redirect:/users";
    }

    @GetMapping("/edit/{id}")
    @ResponseBody // This tells Spring to return the object as JSON, not a template
    public ResponseEntity<User> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        if (user != null) {
            return ResponseEntity.ok(user);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/disable/{id}")
    public String disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return "redirect:/users";
    }
}