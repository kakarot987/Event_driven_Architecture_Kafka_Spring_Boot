package com.eventDrivenKafka.Authentication_Authorization.demo;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class BookstoreController {

    @RequestMapping(value = "/recommended")
    public Mono<String> readingList(){
        return Mono.just("Spring in Action (Manning), Cloud Native Java (O'Reilly), Learning Spring Boot (Packt)");
    }
}
