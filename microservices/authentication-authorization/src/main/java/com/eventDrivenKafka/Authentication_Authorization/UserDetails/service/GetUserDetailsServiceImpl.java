package com.eventDrivenKafka.Authentication_Authorization.UserDetails.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.CreateProfileRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.GetUserDetails;
import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.UserDetailsResponse;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RestController;

import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetails;
import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetailsRepository;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.UserRepository;


@RestController
public class GetUserDetailsServiceImpl implements GetUserDetails {
	private static final Logger logger = LoggerFactory.getLogger(GetUserDetailsServiceImpl.class);

	private final UserDetailsRepository repo;
	private final GetUserRoles getUserRoles;
	private final CreateProfileService createProfileService;
	private final UserRepository userRepository;

	public GetUserDetailsServiceImpl(UserDetailsRepository repo, GetUserRoles getUserRoles,
									 CreateProfileService createProfileService, UserRepository userRepository) {
		this.repo = repo;
		this.getUserRoles = getUserRoles;
		this.createProfileService = createProfileService;
		this.userRepository = userRepository;
	}

	List<UserDetails> userDetails;
	List<UserDetailsResponse> responseList = new ArrayList<>();
	private UserDetailsResponse userResponse;
	
	@Override
	public Object getUser(Long userId, String accessToken) throws Exception {
		Optional<User> user = userRepository.findByUserId(userId);
		if(!accessToken.equals(user.get().getAuthToken())) {
			throw new Exception("Unauthorized Access");
		}
		String role;
		try {
			role = getUserRoles.getRoleFromCache(userId);
			logger.info("User role retrieved: {}", role);
		} catch (ExecutionException e) {
			logger.error("Error retrieving user role from cache", e);
		}
		List<Object> responseObj = new ArrayList<>();
		userDetails = repo.getDetails(userId);
		logger.info("Retrieved user details for userId: {}", userId);
		responseObj.add(userDetails);
		return responseObj;
	}

	@Override
	public UserDetailsResponse createProfile(CreateProfileRequest createProfileRequest, Long userId, String accessToken) throws Exception {
		Optional<User> user = userRepository.findByUserId(userId);
		if(!accessToken.equals(user.get().getAuthToken())) {
			throw new Exception("Unauthorized Access");
		}
		return createProfileService.createProfile(userId, createProfileRequest);
	}

}
