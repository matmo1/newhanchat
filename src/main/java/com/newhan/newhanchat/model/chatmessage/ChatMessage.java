package com.newhan.newhanchat.model.chatmessage;

import java.time.LocalDateTime;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "chatmessage")
public class ChatMessage {

    @Id
    private ObjectId mesId;

    private String content;
    
    private ObjectId senderId;
    private ObjectId reciepentId;

    private LocalDateTime timestamp;

    private MessageSatus status = MessageSatus.PENDING;

    public ChatMessage(String content) {
        this.content = content;
    }

    public ObjectId getId() {return mesId;}
    public String getContent() {return content;}
    public ObjectId getSenderId() {return senderId;}
    public ObjectId getReciepentId() {return reciepentId;}
    public LocalDateTime getTimestamp() {return timestamp;}
    public MessageSatus getStatus() {return status;}

    public void setSenderId(ObjectId id) {senderId = id;}
    public void setReciepentId(ObjectId id) {reciepentId = id;}
    public void setStatus(MessageSatus s) {status = s;}
    public void setMessageContent(String content) {this.content = content;}
    public void setTimestamp(LocalDateTime time) {timestamp = time;}
}
