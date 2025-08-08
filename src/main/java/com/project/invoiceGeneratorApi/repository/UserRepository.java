package com.project.invoiceGeneratorApi.repository;

import com.project.invoiceGeneratorApi.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.Optional;

// PURPOSE: Provides data access methods for the User entity to MongoDB.
// Spring Data MongoDB will automatically implement these methods.

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByClerkId(String clerkId);
    boolean existsByClerkId(String clerkId);
}
