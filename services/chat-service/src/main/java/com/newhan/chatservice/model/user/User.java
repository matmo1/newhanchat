package com.newhan.chatservice.model.user;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Document(collection = "users")
public class User {
    @Id
    private ObjectId userId;

    private UserStatus status = new UserStatus();

    @Indexed(unique = true)
    @Size(min = 3, max = 20)
    @NotBlank
    private String userName;
    
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotNull
    private LocalDateTime dateOfBirth;

    @Size(min = 8)
    @NotBlank
    private String password;

    public UserStatus getUserStatus() {return status;}

    public LocalDateTime getLastActive() {return status.getLastActive();}
    public StatusType getStatusType() {return status.getType();}
}
