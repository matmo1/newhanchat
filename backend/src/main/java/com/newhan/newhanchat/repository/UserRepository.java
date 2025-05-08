package com.newhan.newhanchat.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.newhan.newhanchat.model.user.User;

public interface UserRepository extends MongoRepository<User, ObjectId> {

    boolean existsByUserName(String userName);

}
