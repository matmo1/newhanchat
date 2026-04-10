package com.newhan.chatservice.repository;

import com.newhan.chatservice.model.user.StatusType;
import com.newhan.chatservice.model.user.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends MongoRepository<User, String> {
    // Find by the username field
    Optional<User> findByNickName(String nickName);
    
    // Find by online status
    List<User> findAllByStatus_Type(StatusType statusType);
}