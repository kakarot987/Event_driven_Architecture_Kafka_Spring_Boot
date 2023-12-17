package com.eventDrivenKafka.Authentication_Authorization.service;

import com.eventDrivenKafka.Authentication_Authorization.dto.SocialLoginRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.UserDTO;
import com.eventDrivenKafka.Authentication_Authorization.persistence.UserRepository;
import org.apache.catalina.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public void registerUser(UserDTO userDTO) {
        // Validate userDTO, perform business logic, and save to UserRepository
    }

    // Other user-related methods (e.g., getUserById, updateUser, etc.)
    public UserDTO registerSocialUser(SocialLoginRequest socialLoginRequest) {
        // Validate social registration request, perform business logic, and save to UserRepository
        // Return the registered user
//        return userRepository.save(new User()); // Example, you should implement this method according to your business logic
        return null;
    }
}
