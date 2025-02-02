package com.eventDrivenKafka.Authentication_Authorization.UserDetails.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.CreateProfileRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.GetUserDetails;
import com.eventDrivenKafka.Authentication_Authorization.dto.userDetails.UserDetailsResponse;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RestController;

import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetails;
import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetailsRepository;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.UserRepository;


@RestController
public class GetUserDetailsServiceImpl implements GetUserDetails {

	@Autowired
	private UserDetailsRepository repo;
	
	@Autowired
	private GetUserRoles getuserRoles;
	
	@Autowired
	private CreateProfileService createProfileService;
	
	@Autowired
	private UserRepository userRepository;

	List<UserDetails> userDetails;
	List<UserDetailsResponse> responselist = new ArrayList<>();
	private UserDetailsResponse user_response;
	
//	@PreAuthorize("hasRole('USER')")
	@Override
	public Object getUser(Long userId, String accessToken) throws Exception {
		Optional<User> user = userRepository.findByUserId(userId);
		if(!accessToken.equals(user.get().getAuthToken())) {
			throw new Exception("Unauthorized Access");
		}
		String role;
		try {
			role = getuserRoles.getRoleFromCache(userId);
			System.out.print("--------------"+role);
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		// TODO Auto-generated method stub
		List<Object> responseobj = new ArrayList<>();
		userDetails = repo.getDetails(userId);
		System.out.println("userDetails");
		System.out.println(userDetails);
		responseobj.add(userDetails);
		return responseobj;

	}

	@Override
	public UserDetailsResponse createProfile(CreateProfileRequest createProfileRequest, Long userId, String accessToken) throws Exception {
		// TODO Auto-generated method stub
		Optional<User> user = userRepository.findByUserId(userId);
		if(!accessToken.equals(user.get().getAuthToken())) {
			throw new Exception("Unauthorized Access");
		}
		
		return createProfileService.createProfile(userId, createProfileRequest);
	}

}
