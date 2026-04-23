package com.newhan.userservice.service;

import com.newhan.userservice.dto.UserLoginDTO;
import com.newhan.userservice.dto.UserRegistrationDTO;
import com.newhan.userservice.dto.UserResponseDTO;
import com.newhan.userservice.dto.UserUpdateEvent;
import com.newhan.userservice.model.User;
import com.newhan.userservice.repository.UserRepository;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Value;
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

    @Value("${app.api-base-url:http://10.73.230.97:8082}")
    private String apiBaseUrl;

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
        
        // Generate a nice default profile picture using their initials!
        String defaultAvatarUrl = "https://ui-avatars.com/api/?name=" + dto.fname() + "+" + dto.lname() + "&background=random";
        user.setProfilePictureUrl(defaultAvatarUrl);
        
        // Handle Date parsing carefully and flexibly
        try {
            if (dto.dOfBirth() != null) {
                String dobString = dto.dOfBirth().toString();
                // If the frontend sends "1995-10-25" without the time, this will safely append the time so ISO parsing doesn't crash!
                if (!dobString.contains("T")) {
                    dobString += "T00:00:00"; 
                }
                DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
                user.setDateOfBirth(LocalDateTime.parse(dobString, formatter));
            }
        } catch (DateTimeParseException e) {
            throw new RuntimeException("Invalid date format. Expected yyyy-MM-dd'T'HH:mm:ss", e);
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

    // --- Internal Methods returning raw User entity ---
    
    public User getProfile(String userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }
    
    public User getProfileByUsername(String username) {
         return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // --- Endpoint Methods returning secure UserResponseDTO ---

    public UserResponseDTO getProfileDTO(String userId) {
        User user = getProfile(userId);
        return mapToDTO(user);
    }

    public List<UserResponseDTO> getAllUsersDTO() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .toList();
    }

    public UserResponseDTO updateBio(String userId, String newBio) {
        User user = getProfile(userId);
        user.setBio(newBio);
        User savedUser = userRepository.save(user);
        return mapToDTO(savedUser);
    }

    public UserResponseDTO updateProfilePicture(String userId, MultipartFile file, HttpServletRequest request) {
        // 1. Fetch the user first to ensure they exist
        User user = getProfile(userId);

        // 2. Save physical file to disk
        String fileName = picStorageService.storeFile(file);

        user.setProfilePictureUrl(fileName);
        User savedUser = userRepository.save(user);

        String fullName = savedUser.getFirstName() + " " + savedUser.getLastName();
        
        // ✨ FIXED: Pass only the raw filename to Kafka as well!
        kafkaProducerService.sendProfileUpdate(new UserUpdateEvent(
            savedUser.getId(), 
            savedUser.getUsername(), 
            fullName, 
            fileName 
        ));

        return mapToDTO(savedUser);
    }

    public UserResponseDTO updateName(String userId, String fname, String lname) {
        User user = getProfile(userId);
        user.setFirstName(fname);
        user.setLastName(lname);
        User savedUser = userRepository.save(user);

        // Notify Chat Service via Kafka so their new name shows up in chats immediately
        String fullName = savedUser.getFirstName() + " " + savedUser.getLastName();
        kafkaProducerService.sendProfileUpdate(new UserUpdateEvent(
            savedUser.getId(), 
            savedUser.getUsername(), 
            fullName, 
            savedUser.getProfilePictureUrl()
        ));

        return mapToDTO(savedUser);
    }

    // --- Helper DTO Mapper ---
    
    private UserResponseDTO mapToDTO(User user) {
        return new UserResponseDTO(
            user.getId(), user.getUsername(), user.getFirstName(), 
            user.getLastName(), user.getStatus(), user.getBio(), user.getProfilePictureUrl()
        );
    }
}