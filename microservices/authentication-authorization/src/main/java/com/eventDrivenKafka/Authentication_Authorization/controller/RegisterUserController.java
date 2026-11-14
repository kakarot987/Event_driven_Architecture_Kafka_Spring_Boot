package com.eventDrivenKafka.Authentication_Authorization.controller;

import com.eventDrivenKafka.Authentication_Authorization.UserAggregate;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.RequestPasswordResetRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.ResetPasswordRequest;
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

	@Operation(summary = "Request password reset")
	@PostMapping("register-login/request-password-reset")
	ApisResponse requestPasswordReset(@RequestBody RequestPasswordResetRequest request);

	@Operation(summary = "Redeem one-time password reset token")
	@PostMapping("register-login/reset-password")
	ApisResponse resetPassword(@RequestBody ResetPasswordRequest request);
}
