package com.eventDrivenKafka.authenticationserver.service;

import com.eventDrivenKafka.authenticationserver.payload.SocialLoginRequest;
import com.eventDrivenKafka.authenticationserver.payload.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/user/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);

    @GetMapping("/user/social/{socialId}")
    UserDTO getUserBySocialId(@PathVariable("socialId") String socialId);

    @PostMapping("/user/register/social")
    UserDTO registerSocialUser(@RequestBody SocialLoginRequest socialLoginRequest);
}

