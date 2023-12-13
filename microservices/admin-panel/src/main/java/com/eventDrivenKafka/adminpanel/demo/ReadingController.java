package com.eventDrivenKafka.adminpanel.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@RestController
public class ReadingController {
    @RequestMapping("/to-read")
public Mono<String> toRead() {
    return WebClient.builder().build()
            .get().uri("http://localhost:8080/recommended").retrieve()
            .bodyToMono(String.class);
}
}
