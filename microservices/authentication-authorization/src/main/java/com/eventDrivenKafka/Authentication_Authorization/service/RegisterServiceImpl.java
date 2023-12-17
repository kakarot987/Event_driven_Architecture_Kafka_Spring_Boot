package com.eventDrivenKafka.Authentication_Authorization.service;

import com.eventDrivenKafka.Authentication_Authorization.persistence.*;
import com.eventDrivenKafka.api.auth_auth.RegisterService;
import com.eventDrivenKafka.api.auth_auth.payload.request.SignInRequest;
import com.eventDrivenKafka.api.auth_auth.payload.request.SignupRequest;
import com.eventDrivenKafka.api.auth_auth.payload.request.UserAggregate;
import com.eventDrivenKafka.api.auth_auth.payload.response.JwtResponse;
import com.eventDrivenKafka.util.http.ServiceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
public class RegisterServiceImpl implements RegisterService {

	private static final Logger LOG = LoggerFactory.getLogger(RegisterServiceImpl.class);

	private UserRepository repository;

	private PasswordEncoder encoder;

	private AuthService authService;

	private final ServiceUtil serviceUtil;
	private RoleRepository roleRepository;

	@Autowired
	public RegisterServiceImpl(UserRepository repository, AuthService authService, PasswordEncoder encoder,
			ServiceUtil serviceUtil,
			RoleRepository roleRepository) {
		this.repository = repository;
		this.authService = authService;
		this.encoder = encoder;
		this.serviceUtil = serviceUtil;
		this.roleRepository = roleRepository;
	}

	@Override
	public UserAggregate registerUser(SignupRequest signUpRequest) throws Exception {
		// TODO Auto-generated method stub
		Boolean varified = false, verifiedMobile = false;
		Date emailOtpModifiedDate = null, emailOtpCreatedDate = null;
		Date mobileOtpModifiedDate = null, mobileOtpCreatedDate = null;
		String verifiedOtp = null;
		String varifiedMobileOtp = null;

		String password = signUpRequest.getPassword();
		Date createdDate = new Date();
		Random random = new Random();
		int userId = 100 + random.nextInt(900000);

		//user status 1 for active user and other than 1 user will be considered as deactivated
		int userStatus = 1;
		
		User user = new User(Long.valueOf(userId), "jwt", signUpRequest.getEmail(), signUpRequest.getPhoneNumber(),
				encoder.encode(password), varified, verifiedOtp, emailOtpCreatedDate,
				emailOtpModifiedDate, varifiedMobileOtp, 
				mobileOtpCreatedDate, mobileOtpModifiedDate, verifiedMobile, createdDate,userStatus);

		Set<Role> roles = new HashSet<>();
		Role userRole = roleRepository.findByName(ERole.USER);
		System.out.println(userRole + "------------");
		roles.add(userRole);
		user.setRoles(roles);

		User user1 = repository.save(user);

		Set<Role> roleResponse = new HashSet<>();
		roleResponse = user1.getRoles();
		Set<String> roleString = new HashSet<>();
		for (Role r : roleResponse) {
			roleString.add(r.toString());
		}

		JwtResponse jwtResponse = authService.updateUserAuth(user1.getUserId(), password);

		UserAggregate p = new UserAggregate(user1.getUserId(), jwtResponse.getAccessToken(), createdDate, createdDate,
				jwtResponse.getRoles());
		return p;
	}

	@Override
	public UserAggregate loginUser(SignInRequest signInRequest) throws Exception {
		if (signInRequest.getEmail() == null) {
			Optional<User> user = repository.findByPhoneNumber(signInRequest.getPhoneNumber());
			if (user.get().getFlagActive() != 1) {
				throw new Exception("Deactivate Account");
			}
			JwtResponse jwtResponse = authService.updateUserAuth(user.get().getUserId(), signInRequest.getPassword());
			UserAggregate p = new UserAggregate(user.get().getUserId(), jwtResponse.getAccessToken(),
					user.get().getCreated(), jwtResponse.getModifiedDate(), jwtResponse.getRoles());
			return p;
		}
		if (signInRequest.getPhoneNumber() == null) {
			Optional<User> user = repository.findByEmail(signInRequest.getEmail());
			if (user.get().getFlagActive() != 1) {
				throw new Exception("Deactivate Account");
			}

			JwtResponse jwtResponse = authService.updateUserAuth(user.get().getUserId(), signInRequest.getPassword());

			UserAggregate p = new UserAggregate(user.get().getUserId(), jwtResponse.getAccessToken(),
					user.get().getCreated(), jwtResponse.getModifiedDate(), jwtResponse.getRoles());
			return p;
		}
		return null;
	}
	
	
}
