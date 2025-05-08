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
    private ObjectId recipientId;

    private LocalDateTime timestamp;

    private MessageSatus status = MessageSatus.PENDING;

    private boolean edited = false;
    private LocalDateTime lastEdited;

    public ChatMessage(String content) {
        this.content = content;
    }

    public ObjectId getId() {return mesId;}
    public String getContent() {return content;}
    public ObjectId getSenderId() {return senderId;}
    public ObjectId getReciepentId() {return recipientId;}
    public LocalDateTime getTimestamp() {return timestamp;}
    public MessageSatus getStatus() {return status;}
    public boolean getEdited() {return edited;}
    public LocalDateTime getLastEdited() {return lastEdited;}

    public void setSenderId(ObjectId id) {senderId = id;}
    public void setReciepentId(ObjectId id) {recipientId = id;}
    public void setStatus(MessageSatus s) {status = s;}
    public void setMessageContent(String content) {this.content = content;}
    public void setTimestamp(LocalDateTime time) {timestamp = time;}
    public void setEdited(boolean b) {edited = b;}
    public void setLastEdited() {lastEdited = LocalDateTime.now();} 
}
