package com.project.invoiceGeneratorApi.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;

// PURPOSE: Defines the structure of our user document in MongoDB.

@Data
@Document(collection = "users")
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    private String id;   // MongoDB's _id

    @Indexed(unique = true)
    private String clerkId;  // Clerk's user ID
    private String email;
    private String firstName;
    private String lastName;
    private String photoUrl;
    @CreatedDate
    private Instant createdAt;
}
