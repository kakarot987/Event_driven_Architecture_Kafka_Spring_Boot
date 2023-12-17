package com.eventDrivenKafka.Authentication_Authorization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
	Role findByName(ERole name);
}
