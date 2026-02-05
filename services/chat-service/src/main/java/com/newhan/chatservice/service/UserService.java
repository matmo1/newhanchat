package com.newhan.chatservice.service;

import com.newhan.chatservice.model.user.StatusType;
import com.newhan.chatservice.model.user.User;
import com.newhan.chatservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public void connect(User user) {
        // Find by Nickname, OR create new if first time in Chat Service
        var storedUser = userRepository.findByNickName(user.getNickName())
                                       .orElse(user);
        
        storedUser.getStatus().setType(StatusType.ONLINE);
        // Ensure other fields are up to date
        storedUser.setFullName(user.getFullName());
        storedUser.setProfilePictureUrl(user.getProfilePictureUrl());
        
        userRepository.save(storedUser);
    }

    public void disconnect(String nickName) {
        var storedUser = userRepository.findByNickName(nickName).orElse(null);
        if (storedUser != null) {
            storedUser.getStatus().setType(StatusType.OFFLINE);
            userRepository.save(storedUser);
        }
    }

    public List<User> findConnectedUsers() {
        return userRepository.findAllByStatus_Type(StatusType.ONLINE);
    }
}