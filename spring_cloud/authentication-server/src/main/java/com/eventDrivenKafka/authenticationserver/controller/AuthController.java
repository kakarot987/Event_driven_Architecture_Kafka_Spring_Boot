package com.eventDrivenKafka.authenticationserver.controller;

import com.eventDrivenKafka.authenticationserver.payload.LoginRequest;
import com.eventDrivenKafka.authenticationserver.payload.SocialLoginRequest;
import com.eventDrivenKafka.authenticationserver.service.AuthService;
import com.eventDrivenKafka.authenticationserver.service.SocialAuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;
    @Autowired
    private SocialAuthService socialAuthService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody LoginRequest loginRequest) {
        String token = authService.authenticateUser(loginRequest);
        return ResponseEntity.ok(token);
    }

    @PostMapping("/social-login")
    public ResponseEntity<String> socialLogin(@RequestBody SocialLoginRequest socialLoginRequest) {
        String token = socialAuthService.authenticateWithSocialProvider(socialLoginRequest);
        return ResponseEntity.ok(token);
    }
}
