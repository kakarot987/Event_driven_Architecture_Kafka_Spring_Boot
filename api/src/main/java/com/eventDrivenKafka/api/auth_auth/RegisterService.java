package com.eventDrivenKafka.api.auth_auth;

import com.eventDrivenKafka.api.auth_auth.payload.request.SignInRequest;
import com.eventDrivenKafka.api.auth_auth.payload.request.SignupRequest;
import com.eventDrivenKafka.api.auth_auth.payload.request.UserAggregate;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Api(description = "API to register User.")
public interface RegisterService {

    @ApiResponses(value = {
            @ApiResponse(code = 400, message = "Bad Request, invalid format of the request. See response message for more information."),
            @ApiResponse(code = 404, message = "Not found, the specified id does not exist."),
            @ApiResponse(code = 422, message = "Unprocessable entity, input parameters caused the processing to fail. See response message for more information.")
    })
    @PostMapping(value = "register-login/signup",
            produces = "application/json")
    public UserAggregate registerUser(@RequestBody SignupRequest signUpRequest) throws Exception;

    @PostMapping(value = "register-login/signin")
    public UserAggregate loginUser(@RequestBody SignInRequest signInRequest) throws Exception;
}