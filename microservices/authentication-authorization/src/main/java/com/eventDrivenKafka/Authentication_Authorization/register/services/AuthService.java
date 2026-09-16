package com.eventDrivenKafka.Authentication_Authorization.register.services;

import jakarta.transaction.Transactional;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.eventDrivenKafka.Authentication_Authorization.register.persistence.User;
import com.eventDrivenKafka.Authentication_Authorization.register.persistence.UserRepository;
import com.eventDrivenKafka.Authentication_Authorization.register.util.JwtUtils;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.JwtResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthService {
	private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

	private final UserRepository userRepository;
	private final AuthenticationManager authenticationManager;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtils jwtUtils;

	public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager,
					   PasswordEncoder passwordEncoder, JwtUtils jwtUtils) {
		this.userRepository = userRepository;
		this.authenticationManager = authenticationManager;
		this.passwordEncoder = passwordEncoder;
		this.jwtUtils = jwtUtils;
	}

	@Transactional
	public JwtResponse updateUserAuth(Long userId, String password){
		JwtRes jwt = generateToken(userId, password);
		Date modifiedDate = new Date();
		userRepository.updateUserSaveToken(userId, modifiedDate, jwt.getJwt());
		logger.info("User auth updated for userId: {}", userId);
		return new JwtResponse(userId, jwt.getJwt(), jwt.getUsername(), jwt.getRoles());
	}
	
	public boolean changePassword(String email, String password) {
		User user = findByEmail(email);
		user.setPassword(passwordEncoder.encode(password));
		if(userRepository.save(user) != null) {
			logger.info("Password changed for email: {}", email);
			return true;
		}
		return false;
	}
	
	public boolean changePasswordByMobile(Long phoneNumber, String password) {
		User user = findByPhoneNumber(phoneNumber);
		user.setPassword(passwordEncoder.encode(password));
		if(userRepository.save(user) != null) {
			logger.info("Password changed for phone: {}", phoneNumber);
			return true;
		}
		return false;
	}

	private User findByPhoneNumber(Long phoneNumber) {
		return userRepository.findByPhoneNumber(phoneNumber).get();
	}

	public User findByEmail(String email) {
		return userRepository.findByEmail(email).get();        
	}
	
	private JwtRes generateToken(Long userId, String password){
		String username = String.valueOf(userId);
		Authentication authentication = authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(username, password));

		SecurityContextHolder.getContext().setAuthentication(authentication);
		String jwt = jwtUtils.generateJwtToken(authentication);
		UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
		List<String> roles = userDetails.getAuthorities().stream()
				.map(item -> item.getAuthority())
				.collect(Collectors.toList());

		return new JwtRes(jwt, roles, userDetails.getUsername());
	}
}

class JwtRes {
	private String jwt;
	private List<String> roles;
	private String username;

	public JwtRes(String jwt, List<String> roles, String username) {
		this.jwt = jwt;
		this.roles = roles;
		this.username = username;
	}

	public String getJwt() {
		return jwt;
	}

	public void setJwt(String jwt) {
		this.jwt = jwt;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}
}