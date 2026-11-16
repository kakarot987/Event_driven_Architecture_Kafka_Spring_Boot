package com.eventDrivenKafka.Authentication_Authorization.controller;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.LoginRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.request.VerifyMobileRequest;
import com.eventDrivenKafka.Authentication_Authorization.dto.registerLogin.payload.response.ApisResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.RestController;

// Legacy SMS OTP logged recovery codes and permitted account takeover without ownership proof.
// Keep the API disabled until a separately verified, single-use flow is implemented.
@RestController
public class OtpServiceImpl implements OtpMsgController {
    private ResponseStatusException disabled() {
        return new ResponseStatusException(HttpStatus.GONE, "Mobile OTP and password recovery are disabled");
    }
    @Override public ApisResponse sendOptToMobileNumber(String number) { throw disabled(); }
    @Override public ApisResponse verifySentOtp(String number, String sentOtp) { throw disabled(); }
    @Override public ApisResponse resetPasswordByMobile(LoginRequest req) { throw disabled(); }
    @Override public ApisResponse generateOtpForPassReset(VerifyMobileRequest req) { throw disabled(); }
    @Override public ApisResponse verifyOtpForPassReset(VerifyMobileRequest req) { throw disabled(); }
}
