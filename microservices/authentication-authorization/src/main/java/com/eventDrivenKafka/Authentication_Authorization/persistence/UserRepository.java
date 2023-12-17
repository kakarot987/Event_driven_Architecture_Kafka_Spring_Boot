package com.eventDrivenKafka.Authentication_Authorization.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	Optional<User> findByUserId(Long userId);

	Optional<User> findByPhoneNumber(Long phoneNumber);
	
	Boolean existsByUserId(Long userId);
	
	Boolean existsByEmail(String email);

	Boolean existsByPhoneNumber(Long phoneNumber);
    
	Optional<User> findByEmail(String email);
	

	  @Modifying
	  @Query("update User u set u.authToken = :authToken, u.modified = :modified where u.userId = :userId"
	  ) void updateUserSaveToken(@Param("userId") Long
	  userId, @Param("modified")Date modified, @Param("authToken") String
	  authToken);
	  
}