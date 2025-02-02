package com.eventDrivenKafka.Authentication_Authorization.dto.userDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
public interface GetUserDetails {
	
	
	@GetMapping(value = "register-login/getUserDetails",
			produces = "application/json")
	public Object getUser(@RequestHeader Long userId,@RequestHeader String accessToken) throws Exception;
	
	
	@PostMapping(value = "register-login/createProfile",
			produces = "application/json")
	public UserDetailsResponse createProfile(@RequestBody CreateProfileRequest createProfileRequest,@RequestHeader Long userId,@RequestHeader String accessToken) throws Exception;

}
