package com.newhan.chatservice.service;

import com.newhan.chatservice.model.user.StatusType; // Ensure you have this enum
import com.newhan.chatservice.model.user.User;
import com.newhan.chatservice.repository.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // --- KEPT: Logic for Chat Status (Online/Offline) ---

    public void connect(User user) {
        user.setStatus(StatusType.ONLINE);
        userRepository.save(user);
    }

    public void disconnect(User user) {
        var storedUser = userRepository.findById(user.getNickName()).orElse(null);
        if (storedUser != null) {
            storedUser.setStatus(StatusType.OFFLINE);
            userRepository.save(storedUser);
        }
    }

    public List<User> findConnectedUsers() {
        return userRepository.findAllByStatus(StatusType.ONLINE);
    }
}