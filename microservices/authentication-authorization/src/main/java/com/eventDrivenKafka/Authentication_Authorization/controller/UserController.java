package com.eventDrivenKafka.Authentication_Authorization.controller;

import com.eventDrivenKafka.Authentication_Authorization.dto.SocialLoginRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.UserDTO;
import com.eventDrivenKafka.Authentication_Authorization.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);
        return ResponseEntity.ok("User registered successfully.");
    }

    @PostMapping("/register/social")
    public ResponseEntity<String> registerSocialUser(@RequestBody SocialLoginRequest socialLoginRequest) {
        UserDTO userDTO = userService.registerSocialUser(socialLoginRequest);
        return ResponseEntity.ok("User registered successfully.");
    }
}

