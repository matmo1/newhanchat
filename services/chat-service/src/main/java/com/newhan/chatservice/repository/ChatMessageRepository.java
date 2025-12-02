package com.newhan.chatservice.repository;

import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.newhan.chatservice.model.chatmessage.ChatMessage;

import java.util.List;

public interface ChatMessageRepository extends MongoRepository<ChatMessage, ObjectId> {

    @Query("{$or: [ { 'senderId': ?0, 'recipientId': ?1 }, { 'senderId': ?1, 'recipientId': ?0 } ]}")
    List<ChatMessage> findChatHistory(ObjectId user1, ObjectId user2);
}
