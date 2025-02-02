package com.eventDrivenKafka.Authentication_Authorization.register.config;

import com.eventDrivenKafka.Authentication_Authorization.register.persistence.ERole;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.Role;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static com.eventDrivenKafka.Authentication_Authorization.register.persistence.ERole.ROLE_ADMIN;
import static com.eventDrivenKafka.Authentication_Authorization.register.persistence.ERole.ROLE_USER;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    RoleRepository roleRepository;

    @Override
    public void run(String... args) throws Exception {
        // Perform the data insertion here

        List<Role> roles = new ArrayList<>();
        Role role = new Role(ROLE_USER);
        roles.add(role);
        roles.add(new Role(ROLE_ADMIN));
        for (Role e : roles){
            if(!roleRepository.existsByName(e.getName())){
                roleRepository.saveAll(roles);
            }
        }
    }
}