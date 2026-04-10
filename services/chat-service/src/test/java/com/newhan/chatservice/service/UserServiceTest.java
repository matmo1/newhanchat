package com.newhan.chatservice.service;

import com.newhan.chatservice.model.user.StatusType;
import com.newhan.chatservice.model.user.User;
import com.newhan.chatservice.model.user.UserStatus;
import com.newhan.chatservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setNickName("johndoe");
        testUser.setFullName("John Doe");
        testUser.setStatus(new UserStatus());
    }

    @Test
    void connect_NewUser_SetsOnlineAndSaves() {
        when(userRepository.findByNickName("johndoe")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.connect(testUser);

        verify(userRepository).save(argThat(user -> 
            user.getNickName().equals("johndoe") && 
            user.getStatus().getType() == StatusType.ONLINE
        ));
    }

    @Test
    void connect_ExistingUser_UpdatesStatus() {
        User existingUser = new User();
        existingUser.setNickName("johndoe");
        existingUser.setStatus(new UserStatus());
        existingUser.getStatus().setType(StatusType.OFFLINE);

        when(userRepository.findByNickName("johndoe")).thenReturn(Optional.of(existingUser));

        userService.connect(testUser);

        verify(userRepository).save(argThat(user -> 
            user.getStatus().getType() == StatusType.ONLINE
        ));
    }

    @Test
    void disconnect_ExistingUser_SetsOffline() {
        testUser.getStatus().setType(StatusType.ONLINE);
        when(userRepository.findByNickName("johndoe")).thenReturn(Optional.of(testUser));

        userService.disconnect("johndoe");

        verify(userRepository).save(argThat(user -> 
            user.getNickName().equals("johndoe") && 
            user.getStatus().getType() == StatusType.OFFLINE
        ));
    }

    @Test
    void findConnectedUsers_ReturnsOnlyOnlineUsers() {
        when(userRepository.findAllByStatus_Type(StatusType.ONLINE)).thenReturn(List.of(testUser));

        List<User> result = userService.findConnectedUsers();

        assertEquals(1, result.size());
        assertEquals("johndoe", result.get(0).getNickName());
    }
}