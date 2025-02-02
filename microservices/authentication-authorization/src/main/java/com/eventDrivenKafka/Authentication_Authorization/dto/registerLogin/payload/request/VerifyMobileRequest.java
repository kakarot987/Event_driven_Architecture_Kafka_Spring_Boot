package com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request;

import jakarta.validation.constraints.NotBlank;

public class VerifyMobileRequest {
    
	@NotBlank
	private Long phoneNumber;
	
	private Long mobileOtp;

	public Long getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(Long phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public Long getMobileOtp() {
		return mobileOtp;
	}

	public void setMobileOtp(Long mobileOtp) {
		this.mobileOtp = mobileOtp;
	}
	
}
