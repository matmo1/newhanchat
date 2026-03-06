package com.newhan.userservice.service;

import com.newhan.userservice.dto.UserLoginDTO;
import com.newhan.userservice.dto.UserRegistrationDTO;
import com.newhan.userservice.dto.UserUpdateEvent;
import com.newhan.userservice.model.User;
import com.newhan.userservice.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final KafkaProducerService kafkaProducerService;
    private final ProfilePicStorageService picStorageService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, KafkaProducerService kafkaProducerService, ProfilePicStorageService picStorageService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.kafkaProducerService = kafkaProducerService;
        this.picStorageService = picStorageService;
    }

    public void register(UserRegistrationDTO dto) {
        if (userRepository.existsByUsername(dto.userName())) {
            throw new RuntimeException("Username already exists");
        }
        User user = new User();
        user.setUsername(dto.userName());
        user.setFirstName(dto.fname());
        user.setLastName(dto.lname());
        user.setPassword(passwordEncoder.encode(dto.password()));
        
        // Handle Date parsing carefully
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
            user.setDateOfBirth(LocalDateTime.parse(dto.dOfBirth().toString(), formatter));
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format. Expected yyyy-MM-dd'T'HH:mm:ss");
        }
        
        userRepository.save(user);
    }

    public String login(UserLoginDTO dto) {
        User user = userRepository.findByUsername(dto.username())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(dto.password(), user.getPassword())) {
            throw new RuntimeException("Invalid credentials");
        }

        user.setLastSeen(LocalDateTime.now());
        userRepository.save(user);

        return jwtService.generateToken(user.getUsername(), user.getId());
    }

    public User getProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    // Helper to get ID by Username
    public User getProfileByUsername(String username) {
         return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateBio(String userId, String newBio) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setBio(newBio);
        return userRepository.save(user);
    }

    public User updateProfilePicture(String userId, MultipartFile file, HttpServletRequest request) {
        // 1. Fetch the user first to ensure they exist before doing expensive file IO
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Save physical file to disk
        String fileName = picStorageService.storeFile(file);

        // 3. Dynamically build the URL based on the request origin
        String baseUrl = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort();
        String fileUrl = baseUrl + "/api/users/media/" + fileName;

        // 4. Update database
        user.setProfilePictureUrl(fileUrl);
        User savedUser = userRepository.save(user);

        // 5. Notify Chat Service via Kafka
        String fullName = savedUser.getFirstName() + " " + savedUser.getLastName();
        kafkaProducerService.sendProfileUpdate(new UserUpdateEvent(savedUser.getUsername(), fullName, fileUrl));

        return savedUser;
    }
}