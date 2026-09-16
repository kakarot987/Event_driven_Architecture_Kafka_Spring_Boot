package com.eventDrivenKafka.Authentication_Authorization.UserDetails.service;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetails;
import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetailsRepository;
import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.CreateProfileRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.UserDetailsResponse;

@Service
public class CreateProfileService {
	private final UserDetailsRepository repo;

	public CreateProfileService(UserDetailsRepository repo) {
		this.repo = repo;
	}

	@Transactional
	public UserDetailsResponse createProfile(Long userId, CreateProfileRequest createProfileRequest) {

		UserDetails userDetails = repo.createProfile(userId,
				createProfileRequest.getFirstname(), createProfileRequest.getLastname(),
				createProfileRequest.getGender(), createProfileRequest.getCountry());
		UserDetailsResponse userDetailsResponse = new UserDetailsResponse(
				userDetails.getUserId(), userDetails.getEmail(), userDetails.getUserName(),
				userDetails.getFirstName(), userDetails.getLastName(), userDetails.getGender(),
				userDetails.getCountry());
				
		return userDetailsResponse;		
	}
}
