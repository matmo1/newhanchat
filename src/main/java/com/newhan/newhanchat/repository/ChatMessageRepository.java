package com.newhan.newhanchat.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.newhan.newhanchat.model.chatmessage.ChatMessage;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, ObjectId> {

}
