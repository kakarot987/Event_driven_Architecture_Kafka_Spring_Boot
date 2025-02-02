package com.eventDrivenKafka.Authentication_Authorization.controller;

import com.eventDrivenKafka.Authentication_Authorization.UserAggregate;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.LoginRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.SignInRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.SignupRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.ApisResponse;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.JwtResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetails;
import com.eventDrivenKafka.Authentication_Authorization.UserDetails.persistence.UserDetailsRepository;
import com.eventDrivenKafka.Authentication_Authorization.register.services.AuthService;
import com.eventDrivenKafka.Authentication_Authorization.register.services.OtpService;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.ERole;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.Role;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.RoleRepository;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.User;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.UserRepository;

import java.util.Date;
import java.util.HashSet;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

@RestController
@RequestMapping(value = "api/")
@Tag(name = "Register Login Controller")
public class RegisterUserServiceImpl implements RegisterUserController {

	private static final org.slf4j.Logger LOG = org.slf4j.LoggerFactory.getLogger(RegisterUserServiceImpl.class);

	private UserRepository repository;
	private PasswordEncoder encoder;
	private AuthService authService;
	private OtpService otpService;
	private RoleRepository roleRepository;
	private UserDetailsRepository repo;

	@Autowired
	public RegisterUserServiceImpl(UserRepository repository, AuthService authService, PasswordEncoder encoder,
								   UserDetailsRepository repo, OtpService otpService, RoleRepository roleRepository) {
		this.repository = repository;
		this.authService = authService;
		this.encoder = encoder;
		this.otpService = otpService;
		this.roleRepository = roleRepository;
		this.repo = repo;
	}

	@Override
	@Operation(summary = "Register User", description = "Registers a new user.")
	@ApiResponse(responseCode = "201", description = "User successfully registered")
	public UserAggregate registerUser(@RequestBody SignupRequest signUpRequest) throws Exception {

		System.out.println("---------" + signUpRequest.getPassword());
		System.out.println("-------" + signUpRequest.getEmail());
		Boolean verified = false;
		Boolean verifiedMobile = false;

		String password = signUpRequest.getPassword();
		Date createdDate = new Date();
		Random random = new Random();
		int userId = 100 + random.nextInt(900000);

		User user = new User(Long.valueOf(userId), "jwt", signUpRequest.getEmail(), signUpRequest.getPhoneNumber(),
				encoder.encode(password), verified, verifiedMobile, createdDate, signUpRequest.getUsername());

		Set<Role> roles = new HashSet<>();
		Role userRole = roleRepository.findByName(ERole.ROLE_USER);
		System.out.println(userRole + "------------");
		roles.add(userRole);
		user.setRoles(roles);

		User savedUser = repository.save(user);

		if (savedUser.getEmail() == null) {
			UserDetails usr = new UserDetails(savedUser.getUserId(), savedUser.getPhoneNumber(), savedUser.getCreated(), savedUser.getUsername());
			repo.save(usr);
		}

		if (savedUser.getPhoneNumber() == null) {
			UserDetails usr = new UserDetails(savedUser.getUserId(), savedUser.getEmail(), savedUser.getCreated(), savedUser.getUsername());
			repo.save(usr);
		}

		Set<Role> roleResponse = savedUser.getRoles();
		Set<String> roleString = new HashSet<>();
		for (Role r : roleResponse) {
			roleString.add(r.toString());
		}

		JwtResponse jwtResponse = authService.updateUserAuth(savedUser.getUserId(), password);

		UserAggregate userAggregate = new UserAggregate(savedUser.getUserId(), savedUser.getUsername(),
				jwtResponse.getAccessToken(), createdDate, createdDate);
		return userAggregate;
	}

	@Override
	@Operation(summary = "Login User", description = "Logs in a user.")
	@ApiResponse(responseCode = "200", description = "User successfully logged in")
	public UserAggregate loginUser(@RequestBody SignInRequest signInRequest) {

		if (signInRequest.getUsername() != null) {
			System.out.println("here suername" + signInRequest.getUsername());
			Optional<User> user = repository.findByUsername(signInRequest.getUsername());
			JwtResponse jwtResponse = authService.updateUserAuth(user.get().getUserId(), signInRequest.getPassword());
			System.out.println("---" + jwtResponse.getRoles().get(0));
			UserAggregate userAggregate = new UserAggregate(user.get().getUserId(), jwtResponse.getAccessToken(),
					user.get().getUsername(), jwtResponse.getRoles());
			return userAggregate;
		}
		if (signInRequest.getEmail() == null) {
			Optional<User> user = repository.findByPhoneNumber(signInRequest.getPhoneNumber());
			JwtResponse jwtResponse = authService.updateUserAuth(user.get().getUserId(), signInRequest.getPassword());
			Set<Role> roleResponse = user.get().getRoles();
			Set<String> roleString = new HashSet<>();
			for (Role r : roleResponse) {
				roleString.add(r.toString());
			}

			UserAggregate userAggregate = new UserAggregate(user.get().getUserId(), jwtResponse.getAccessToken(),
					user.get().getUsername(), jwtResponse.getRoles());
			return userAggregate;
		}
		if (signInRequest.getPhoneNumber() == null) {
			System.out.println("Here inside" + signInRequest.getEmail() +" "+signInRequest.getPassword());
			Optional<User> user = repository.findByEmail(signInRequest.getEmail());
			System.out.println("Here inside" + user.get().getEmail());
			JwtResponse jwtResponse = authService.updateUserAuth(user.get().getUserId(), signInRequest.getPassword());
			Set<Role> roleResponse = user.get().getRoles();
			Set<String> roleString = new HashSet<>();
			for (Role r : roleResponse) {
				roleString.add(r.toString());
			}

			UserAggregate userAggregate = new UserAggregate(user.get().getUserId(), jwtResponse.getAccessToken(),
					user.get().getUsername(), jwtResponse.getRoles());
			return userAggregate;
		}
		return null;
	}

	@Override
	@Operation(summary = "Reset Password", description = "Resets the password for a user.")
	public ApisResponse resetPassword(@RequestBody LoginRequest loginRequest) {
		if (repository.existsByEmail(loginRequest.getEmail())) {
			if (authService.changePassword(loginRequest.getEmail(), loginRequest.getPassword())) {
				return new ApisResponse(true, "Password changed successfully");
			} else {
				return new ApisResponse(false, "Unable to change password. Try again!");
			}
		} else {
			return new ApisResponse(false, "User not found with this email id");
		}
	}

}
