package com.eventDrivenKafka.Authentication_Authorization.controller;

import com.eventDrivenKafka.Authentication_Authorization.UserAggregate;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.LoginRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.SignInRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.SignupRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.ApisResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping(value = "api/")
@Tag(name = "Register Login Controller")
public interface RegisterUserController {

	@Operation(summary = "Register User", description = "Registers a new user.")
	@ApiResponse(responseCode = "201", description = "User successfully registered",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserAggregate.class)))
	@PostMapping(value = "register-login/signup", produces = "application/json")
	UserAggregate registerUser(@RequestBody @Parameter(description = "Signup Request") SignupRequest signUpRequest) throws Exception;

	@Operation(summary = "Login User", description = "Logs in a user.")
	@ApiResponse(responseCode = "200", description = "User successfully logged in",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = UserAggregate.class)))
	@PostMapping(value = "register-login/signin", produces = "application/json")
	UserAggregate loginUser(@RequestBody @Parameter(description = "Signin Request") SignInRequest signInRequest);

	@Operation(summary = "Reset Password", description = "Resets the password for a user.")
	@PostMapping(value = "register-login/reset-password")
	@ApiResponse(responseCode = "200", description = "Password reset successful",
			content = @Content(mediaType = "application/json", schema = @Schema(implementation = ApisResponse.class)))
	ApisResponse resetPassword(@RequestBody @Parameter(description = "Login Request") LoginRequest loginRequest);
}
