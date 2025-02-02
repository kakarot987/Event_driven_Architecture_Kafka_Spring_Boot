package com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class VerifyEmailRequest {
 
    @NotBlank
    @Email
    private String email;
    
    private Integer otpNo;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getOtpNo() {
        return otpNo;
    }

    public void setOtpNo(Integer otpNo) {
        this.otpNo = otpNo;
    }
    
}