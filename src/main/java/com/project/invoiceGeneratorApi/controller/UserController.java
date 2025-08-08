package com.project.invoiceGeneratorApi.controller;

import com.project.invoiceGeneratorApi.entity.User;
import com.project.invoiceGeneratorApi.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public User createOrUpdate(@RequestBody User userData, Authentication authentication) {

        String authenticatedClerkId = authentication.getName();

        User userToSave = new User();
        userToSave.setClerkId(authenticatedClerkId);
        userToSave.setEmail(userData.getEmail());
        userToSave.setFirstName(userData.getFirstName());
        userToSave.setLastName(userData.getLastName());
        userToSave.setPhotoUrl(userData.getPhotoUrl());

        // The service layer will handle whether to create or update based on this clerkId
        return userService.saveOrUpdateuser(userToSave);
    }
}
