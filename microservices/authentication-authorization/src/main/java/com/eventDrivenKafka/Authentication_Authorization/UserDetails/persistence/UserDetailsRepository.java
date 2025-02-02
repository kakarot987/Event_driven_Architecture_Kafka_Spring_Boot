package com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
@Repository
public interface UserDetailsRepository extends JpaRepository<UserDetails, Integer> {
	@Query("SELECT u FROM UserDetails u WHERE u.userId = :userId")                         
	public List<UserDetails> getDetails(@Param("userId") Long userId);
	
	UserDetails findByUserId(Long userId);
	
	Optional<UserDetails> findByUserName(String username);
	
	@Modifying
	@Query("update UserDetails u set u.firstName = :firstName,"
			+ "u.lastName = :lastName,"
			+ "u.gender = :gender, u.country = :country where u.userId = :userId")
	UserDetails createProfile(@Param("userId") Long userId,
			 @Param("firstName") String firstName, 
			 @Param("lastName") String lastName,
			 @Param("gender") String gender, @Param("country") String country);
}
