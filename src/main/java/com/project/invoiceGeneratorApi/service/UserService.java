package com.project.invoiceGeneratorApi.service;

import com.project.invoiceGeneratorApi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import com.project.invoiceGeneratorApi.entity.User;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;

    public User saveOrUpdateuser(User user) {

        log.info("Attempting to save or update user: {}", user); // Log incoming user

        Optional<User> optionalUser = userRepository.findByClerkId(user.getClerkId());


        if(optionalUser.isPresent()) {

            User existingUser = optionalUser.get();
            log.info("User with clerkId {} found. Updating existing user.", user.getClerkId());

            existingUser.setEmail(user.getEmail());
            existingUser.setFirstName(user.getFirstName());
            existingUser.setLastName(user.getLastName());
            existingUser.setPhotoUrl(user.getPhotoUrl());

            existingUser = userRepository.save(existingUser);
            log.info("Existing user updated: {}", existingUser); // Log updated user
            return existingUser;
        }

        return userRepository.save(user);
    }

    public void deleteAccount(String clerkId) {
        User existingUser = userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        userRepository.delete(existingUser);
    }

    public User getAccountByClerkId(String clerkId) {
        return userRepository.findByClerkId(clerkId)
                .orElseThrow(() -> new RuntimeException("User Not Found."));
    }

}
