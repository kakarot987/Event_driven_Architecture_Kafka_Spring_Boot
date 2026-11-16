package com.eventDrivenKafka.orderservice.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

// Only enabled by isolated local docker-compose core-demo profile.
// Do NOT enable this profile on a network-accessible deployment.
@Configuration
@Profile("core-demo")
public class CoreDemoSecurityConfig {
    @Bean
    SecurityFilterChain coreDemoSecurity(HttpSecurity http) throws Exception {
        return http.csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.requestMatchers("/api/orders/**").permitAll()
                        .anyRequest().denyAll())
                .build();
    }
}
