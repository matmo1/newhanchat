package com.newhan.newhanchat.model.user;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Document(collection = "users")
public class User {
    @Id
    private ObjectId userId;

    private UserStatus status;

    @Indexed(unique = true)
    @Size(min = 3, max = 20)
    @NotBlank
    private String userName;
    
    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @NotBlank
    private LocalDateTime dateOfBirth;

    @Size(min = 8)
    @NotBlank
    private String password;

    public User(String userName, 
        String firstName,
        String lastName, 
        LocalDateTime dateOfBirth, 
        String password) {
            this.userName = userName;
            this.firstName =firstName;
            this.lastName = lastName;
            this.dateOfBirth = dateOfBirth;
            this.password = password;
    }

    public User() {}

    public ObjectId getUserId() {return userId;}
    public String getUserName() {return userName;}
    public String getFirstName() {return firstName;}
    public String getLastName() {return lastName;}
    public UserStatus getUserStatus() {return status;}
    public LocalDateTime getDateOfBirth() {return dateOfBirth;}
    public String getPassword() {return password;}

    public LocalDateTime getLastActive() {return status.getLastActive();}
    public StatusType getStatusType() {return status.getType();}

    public void setUserName(String username) {userName = username;}
    public void setFirstName(String fname) {firstName = fname;}
    public void setLastName(String lname) {lastName =lname;}
    public void setPassword(String pw) {password = pw;}
    
}
