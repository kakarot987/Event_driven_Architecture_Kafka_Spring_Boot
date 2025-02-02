package com.eventDrivenKafka.Authentication_Authorization.controller;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.LoginRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.VerifyMobileRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.ApisResponse;

public interface OtpMsgController {
	
	    @PostMapping("register-login/sendMobileNumber/{number}")
		ApisResponse sendOptToMobileNumber(@PathVariable String number);
		
		@PostMapping("register-login/otpVerify/{number}/{sentOtp}")
		ApisResponse  verifySentOtp(@PathVariable String number, @PathVariable String sentOtp);
		
		@PostMapping("register-login/generateOtpForPassReset")
		ApisResponse  generateOtpForPassReset(@RequestBody VerifyMobileRequest mobileRequest);
		
		@PostMapping("register-login/verifyOtpForPassReset")
		ApisResponse  verifyOtpForPassReset(@RequestBody VerifyMobileRequest mobileRequest);
		
		@PostMapping("register-login/resetPasswordByMobile")
		ApisResponse resetPasswordByMobile(@RequestBody LoginRequest loginRequest);
}

