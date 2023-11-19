package com.eventDrivenKafka.Authentication_Authorization;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthenticationAuthorizationApplication {

	public static void main(String[] args) {
		SpringApplication.run(AuthenticationAuthorizationApplication.class, args);
	}

}
