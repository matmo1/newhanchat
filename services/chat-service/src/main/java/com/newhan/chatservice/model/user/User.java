package com.newhan.chatservice.model.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String id; // Auto-generated Mongo ObjectId (contains timestamp)

    @Indexed(unique = true)
    private String nickName; // The Username (used for lookups)
    
    private String fullName;
    private UserStatus status;
    private String profilePictureUrl;

    public User() {
        this.status = new UserStatus();
    }
}