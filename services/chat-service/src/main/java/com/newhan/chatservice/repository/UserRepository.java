package com.newhan.chatservice.repository;

import java.util.Optional;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import com.newhan.chatservice.model.user.User;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    boolean existsByUserName(String userName);
    Optional<User> findByUserName(String username);
}
