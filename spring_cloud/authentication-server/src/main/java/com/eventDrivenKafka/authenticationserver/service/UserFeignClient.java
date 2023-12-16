package com.eventDrivenKafka.authenticationserver.service;

import com.eventDrivenKafka.authenticationserver.payload.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserFeignClient {

    @GetMapping("/user/{username}")
    UserDTO getUserByUsername(@PathVariable("username") String username);
}

