package com.eventDrivenKafka.authenticationserver.service;

import com.eventDrivenKafka.authenticationserver.payload.LoginRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserFeignClient userFeignClient;

    public String authenticateUser(LoginRequest loginRequest) {
        // Validate credentials, communicate with User Service, and generate JWT token
    }
}

