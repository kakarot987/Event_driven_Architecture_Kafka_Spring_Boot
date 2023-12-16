package com.eventDrivenKafka.Authentication_Authorization.service;

import com.eventDrivenKafka.Authentication_Authorization.dto.UserDTO;
import com.eventDrivenKafka.Authentication_Authorization.repository.UserRepository;
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
}
