package com.newhan.chatservice.model.user;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(collection = "users")
public class User {
    @Id
    private String nickName; // We use username as the ID for Chat Service
    private String fullName;
    private UserStatus status; // ONLINE or OFFLINE
    private String profilePictureUrl; // Good to have for the chat UI
}