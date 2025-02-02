package com.eventDrivenKafka.Authentication_Authorization.register.persistence;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Role findByName(ERole name);
	Boolean existsByName(ERole name);
}
