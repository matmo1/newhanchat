package com.newhan.newhanchat.model;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "users")
public class User {
    @Id
    private ObjectId userId;

    private UserStatus status;

    @Indexed(unique = true)
    private String userName;

    private String firstName;

    private String lastName;

    private LocalDateTime dateOfBirth;

    public User(String userName, String firstName, String lastName, LocalDateTime dateOfBirth) {
        this.userName = userName;
        this.firstName =firstName;
        this.lastName = lastName;
        this.dateOfBirth = dateOfBirth;
    }

    public ObjectId getUserId() {return userId;}
    public String getUserName() {return userName;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public UserStatus getUserStatus() {return status;}
    public LocalDateTime getDateOfBirth() {return dateOfBirth;}

    public LocalDateTime getLastActive() {return status.getLastActive();}
    public StatusType getStatusType() {return status.getType();}
    
}
