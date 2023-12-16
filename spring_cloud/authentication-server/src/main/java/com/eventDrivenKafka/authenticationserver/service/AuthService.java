package com.eventDrivenKafka.authenticationserver.service;

import com.eventDrivenKafka.authenticationserver.payload.LoginRequest;
import com.eventDrivenKafka.authenticationserver.payload.SocialLoginRequest;
import com.eventDrivenKafka.authenticationserver.payload.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserFeignClient userFeignClient;

    public String authenticateUser(LoginRequest loginRequest) {
        // Validate credentials, communicate with User Service, and generate JWT token
        return null;
    }

    public String authenticateWithSocialProvider(SocialLoginRequest socialLoginRequest) {
        // Validate social login credentials
        UserDTO userDTO = new UserDTO(); //userFeignClient.getUserBySocialId(socialLoginRequest.getSocialId());

        // Check if the user is already registered; if not, register the user
        if (userDTO == null) {
            userDTO = userFeignClient.registerSocialUser(socialLoginRequest);
        }

        // Generate JWT token and return it
        return generateJwtToken(userDTO);
    }

    // Implement JWT token generation
    private String generateJwtToken(UserDTO userDTO) {
        // Implementation of JWT token generation
        // ...
        return "your_generated_token";
    }

}

